package com.wonwire.wonwire.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonwire.wonwire.annotation.IntegrationTest;
import com.wonwire.wonwire.dto.LoginRequestDTO;
import com.wonwire.wonwire.dto.RegisterRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests targeting Redis-backed features:
 * - Rate limiting (RateLimitFilter) -> 429 Too Many Requests
 * - JWT blacklist (JwtAuthenticationFilter) -> 401 after logout
 * <p>
 * Isolation strategy:
 * - @DirtiesContext(AFTER_EACH_TEST_METHOD) ensures a clean Spring context
 * (and therefore a fresh embedded Redis + empty H2 DB) for every test.
 * Rate-limit counters are keyed by IP (always 127.0.0.1 in MockMvc), so
 * they would bleed across tests without a full context reset.
 * - Each test uses a unique UUID email as an extra safety net against H2
 * conflicts, in case the isolation strategy ever changes.
 * <p>
 * POST /api/auth/logout without Authorization header -> 401 Unauthorized,
 * handled by MissingRequestHeaderException in GlobalExceptionHandler.
 */
@IntegrationTest
class RedisControllerTest {

    // -------------------------------------------------------------------------
    // Limits -- must mirror RateLimitFilter constants exactly
    // -------------------------------------------------------------------------

    private static final int REGISTER_LIMIT = 3;
    private static final int LOGIN_LIMIT = 5;

    // -------------------------------------------------------------------------
    // Spring beans
    // -------------------------------------------------------------------------

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // -------------------------------------------------------------------------
    // Setup: flush Redis before each test
    // -------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        // Wipe all Redis keys (rate limit counters, blacklisted tokens) to
        // guarantee isolation between tests without restarting the Spring context.
        // H2 data is NOT wiped here -- each test uses a unique UUID email to
        // avoid conflicts with rows already inserted by previous tests.
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    // =========================================================================
    // RATE LIMITING -- /api/auth/register (limit: 3 requests / 60 s)
    // =========================================================================

    @Test
    void rateLimitRegister_ShouldReturn429_WhenLimitExceeded() throws Exception {
        // The first LIMIT requests must go through. Each uses a unique email
        // so none hit a 409 -- only the rate limit filter matters here.
        for (int i = 0; i < REGISTER_LIMIT; i++) {
            final int requestIndex = i;
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRegisterRequest(uniqueEmail()))))
                    .andExpect(result ->
                            org.junit.jupiter.api.Assertions.assertNotEquals(
                                    429, result.getResponse().getStatus(),
                                    "Request #" + requestIndex + " should not be rate-limited yet"
                            ));
        }

        // The (LIMIT + 1)-th request must be blocked.
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRegisterRequest(uniqueEmail()))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Too many requests. Please try again later."));
    }

    @Test
    void rateLimitRegister_ShouldReturn201_WhenUnderLimit() throws Exception {
        // Ensure legitimate requests succeed while under the limit.
        for (int i = 0; i < REGISTER_LIMIT; i++) {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRegisterRequest(uniqueEmail()))))
                    .andExpect(status().isCreated());
        }
    }

    // =========================================================================
    // RATE LIMITING -- /api/auth/login (limit: 5 requests / 60 s)
    // =========================================================================

    @Test
    void rateLimitLogin_ShouldReturn429_WhenLimitExceeded() throws Exception {
        // Register a user to login against, then exhaust the quota.
        // Failed attempts (401) count toward the limit too.
        String email = uniqueEmail();
        registerAndExpect201(email);

        for (int i = 0; i < LOGIN_LIMIT; i++) {
            final int requestIndex = i;
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildLoginRequest(email, "wrongpass" + requestIndex))))
                    .andExpect(result ->
                            org.junit.jupiter.api.Assertions.assertNotEquals(
                                    429, result.getResponse().getStatus(),
                                    "Request #" + requestIndex + " should not be rate-limited yet"
                            ));
        }

        // The (LIMIT + 1)-th request must be blocked.
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildLoginRequest(email, "password123"))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Too many requests. Please try again later."));
    }

    @Test
    void rateLimitLogin_ShouldReturn200_WhenUnderLimit() throws Exception {
        // Legitimate logins succeed while under the quota.
        String email = uniqueEmail();
        registerAndExpect201(email);

        for (int i = 0; i < LOGIN_LIMIT; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildLoginRequest(email, "password123"))))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void rateLimitLogin_ShouldReturn429_AfterMixOfSuccessAndFailure() throws Exception {
        // Key case: the Redis counter increments regardless of whether the
        // request succeeds or fails -- 3 failures + 2 successes = limit reached.
        String email = uniqueEmail();
        registerAndExpect201(email);

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildLoginRequest(email, "badpass"))))
                    .andExpect(status().isUnauthorized());
        }
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildLoginRequest(email, "password123"))))
                    .andExpect(status().isOk());
        }

        // 6th request -- must be blocked.
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildLoginRequest(email, "password123"))))
                .andExpect(status().isTooManyRequests());
    }

    // =========================================================================
    // BLACKLIST -- JWT rejected after logout
    // =========================================================================

    @Test
    void blacklist_ShouldReturn401_WhenTokenUsedAfterLogout() throws Exception {
        String email = uniqueEmail();
        registerAndExpect201(email);

        // 1. Login and obtain a token.
        String token = loginAndGetToken(email);

        // 2. Confirm the token works before logout.
        mockMvc.perform(get("/api/user/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 3. Logout -- token is blacklisted in Redis.
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully logged out"));

        // 4. Reusing the same token must be rejected.
        mockMvc.perform(get("/api/user/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void blacklist_ShouldStillAllowOtherTokens_AfterOneLogout() throws Exception {
        String email = uniqueEmail();
        registerAndExpect201(email);

        // Two separate login sessions -> two distinct tokens.
        // Note: this consumes 2 out of 5 login slots: under the limit.
        String tokenA = loginAndGetToken(email);
        String tokenB = loginAndGetToken(email);

        // Logout token A only.
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        // Token A is now invalid.
        mockMvc.perform(get("/api/user/profile")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isUnauthorized());

        // Token B must still be valid.
        // Each token has its own unique jti, so blacklisting token A's jti has no effect on token B.
        mockMvc.perform(get("/api/user/profile")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk());
    }

    @Test
    void blacklist_ShouldReturn200_AfterReloginPostLogout() throws Exception {
        String email = uniqueEmail();
        registerAndExpect201(email);

        // Logout then re-login -> fresh token must be accepted.
        String oldToken = loginAndGetToken(email);

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + oldToken))
                .andExpect(status().isOk());

        String newToken = loginAndGetToken(email);

        mockMvc.perform(get("/api/user/profile")
                        .header("Authorization", "Bearer " + newToken))
                .andExpect(status().isOk());
    }

    @Test
    void blacklist_LogoutWithoutToken_ShouldReturn401() throws Exception {
        // Missing Authorization header -> 401.
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Generates a unique email for each call to prevent H2 duplicate conflicts.
     */
    private String uniqueEmail() {
        return "test-" + UUID.randomUUID() + "@wonwire.com";
    }

    /**
     * Registers a new user and asserts 201 Created. Consumes one register rate-limit slot.
     */
    private void registerAndExpect201(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRegisterRequest(email))))
                .andExpect(status().isCreated());
    }

    /**
     * Logs in with the given email (password: password123), asserts 200, and returns the JWT.
     * Consumes one login rate-limit slot for the current test context.
     */
    private String loginAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildLoginRequest(email, "password123"))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    private RegisterRequestDTO buildRegisterRequest(String email) {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail(email);
        dto.setPassword("password123");
        dto.setFirstName("Test");
        dto.setLastName("User");
        return dto;
    }

    private LoginRequestDTO buildLoginRequest(String email, String password) {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }
}