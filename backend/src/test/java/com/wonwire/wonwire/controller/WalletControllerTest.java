package com.wonwire.wonwire.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonwire.wonwire.dto.LoginRequestDTO;
import com.wonwire.wonwire.dto.RegisterRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WalletControllerTest {

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
    // GET /api/wallet/balance
    // -------------------------------------------------------------------------

    @Test
    void getBalance_ShouldReturn200WithBalance_WhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/wallet/balance")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.currency").value("KRW"))
                .andExpect(jsonPath("$.email").value("alice@wonwire.com"));
    }

    @Test
    void getBalance_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/wallet/balance"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // POST /api/wallet/deposit
    // -------------------------------------------------------------------------

    @Test
    void deposit_ShouldReturn201AndCreditBalance_WhenRequestIsValid() throws Exception {
        mockMvc.perform(post("/api/wallet/deposit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 50000,
                                    "paymentMethod": "Kakao Pay"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(50000))
                .andExpect(jsonPath("$.newBalance").value(50000))
                .andExpect(jsonPath("$.paymentMethod").value("Kakao Pay"))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void deposit_ShouldReturn400_WhenAmountIsBelowMinimum() throws Exception {
        mockMvc.perform(post("/api/wallet/deposit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 500,
                                    "paymentMethod": "Toss"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deposit_ShouldReturn400_WhenPaymentMethodIsMissing() throws Exception {
        mockMvc.perform(post("/api/wallet/deposit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 50000
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deposit_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/wallet/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 50000,
                                    "paymentMethod": "Toss"
                                }
                                """))
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