package com.wonwire.wonwire.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonwire.wonwire.annotation.IntegrationTest;
import com.wonwire.wonwire.dto.LoginRequestDTO;
import com.wonwire.wonwire.dto.RegisterRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        registerUser("alice@wonwire.com", "Alice", "Smith");
        token = loginAndGetToken("alice@wonwire.com");
    }

    // -------------------------------------------------------------------------
    // GET /api/stats
    // -------------------------------------------------------------------------

    @Test
    @Disabled("Native SQL EXTRACT query not supported by H2 — covered by StatsServiceTest unit tests")
    void getStats_ShouldReturn200WithSixMonths_WhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthly").isArray())
                .andExpect(jsonPath("$.monthly.length()").value(6))
                .andExpect(jsonPath("$.totalSent").value(0))
                .andExpect(jsonPath("$.totalReceived").value(0));
    }

    @Test
    @Disabled("Native SQL EXTRACT query not supported by H2 — covered by StatsServiceTest unit tests")
    void getStats_ShouldReturn200WithCorrectTotals_AfterDepositsAndTransfers() throws Exception {
        // Register a second user
        registerUser("bob@wonwire.com", "Bob", "Martin");
        String bobToken = loginAndGetToken("bob@wonwire.com");

        // Deposit for Alice
        mockMvc.perform(post("/api/wallet/deposit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 100000, "paymentMethod": "Toss"}
                                """))
                .andExpect(status().isCreated());

        // Alice sends to Bob
        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "toEmail": "bob@wonwire.com",
                                    "amount": 30000,
                                    "description": "Test",
                                    "idempotencyKey": "stats-test-001"
                                }
                                """))
                .andExpect(status().isCreated());

        // Alice's stats should show 30000 sent
        mockMvc.perform(get("/api/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSent").value(30000))
                .andExpect(jsonPath("$.totalReceived").value(0));

        // Bob's stats should show 30000 received
        mockMvc.perform(get("/api/stats")
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSent").value(0))
                .andExpect(jsonPath("$.totalReceived").value(30000));
    }

    @Test
    void getStats_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void registerUser(String email, String firstName, String lastName) throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail(email);
        request.setPassword("password123");
        request.setFirstName(firstName);
        request.setLastName(lastName);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    private String loginAndGetToken(String email) throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(email);
        request.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }
}