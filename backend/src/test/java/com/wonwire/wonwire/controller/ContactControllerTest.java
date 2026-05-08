package com.wonwire.wonwire.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonwire.wonwire.dto.LoginRequestDTO;
import com.wonwire.wonwire.dto.RegisterRequestDTO;
import com.wonwire.wonwire.dto.TransferRequestDTO;
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

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String aliceToken;
    private String bobToken;

    @BeforeEach
    void setUp() throws Exception {
        registerUser("alice@wonwire.com", "Alice", "Smith");
        registerUser("bob@wonwire.com", "Bob", "Martin");
        aliceToken = loginAndGetToken("alice@wonwire.com");
        bobToken = loginAndGetToken("bob@wonwire.com");
    }

    // -------------------------------------------------------------------------
    // GET /api/contacts
    // -------------------------------------------------------------------------

    @Test
    void getContacts_ShouldReturn200WithEmptyList_WhenNoTransactions() throws Exception {
        mockMvc.perform(get("/api/contacts")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getContacts_ShouldReturn200WithContact_AfterSendingTransfer() throws Exception {
        // Credit Alice's wallet first
        mockMvc.perform(post("/api/wallet/deposit")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 100000, "paymentMethod": "Toss"}
                                """))
                .andExpect(status().isCreated());

        // Alice sends to Bob
        TransferRequestDTO transfer = new TransferRequestDTO();
        transfer.setToEmail("bob@wonwire.com");
        transfer.setAmount(new BigDecimal("10000"));
        transfer.setDescription("Test");
        transfer.setIdempotencyKey(UUID.randomUUID().toString());

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isCreated());

        // Alice should now have Bob as a contact
        mockMvc.perform(get("/api/contacts")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("bob@wonwire.com"))
                .andExpect(jsonPath("$[0].firstName").value("Bob"))
                .andExpect(jsonPath("$[0].lastName").value("Martin"));
    }

    @Test
    void getContacts_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/contacts"))
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