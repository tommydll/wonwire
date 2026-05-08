package com.wonwire.wonwire.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonwire.wonwire.domain.Wallet;
import com.wonwire.wonwire.dto.LoginRequestDTO;
import com.wonwire.wonwire.dto.RegisterRequestDTO;
import com.wonwire.wonwire.dto.TransferRequestDTO;
import com.wonwire.wonwire.repository.UserRepository;
import com.wonwire.wonwire.repository.WalletRepository;
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
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    private String senderToken;
    private String receiverToken;

    @BeforeEach
    void setUp() throws Exception {
        // Register and login sender
        registerUser("alice@wonwire.com", "Alice", "Dupont");
        senderToken = loginAndGetToken("alice@wonwire.com");

        // Register and login receiver
        registerUser("bob@wonwire.com", "Bob", "Martin");
        receiverToken = loginAndGetToken("bob@wonwire.com");

        creditWallet("alice@wonwire.com", new BigDecimal("500000"));
        creditWallet("bob@wonwire.com", new BigDecimal("500000"));
    }

    // -------------------------------------------------------------------------
    // POST /api/transfers
    // -------------------------------------------------------------------------

    @Test
    void transfer_ShouldReturn201_WhenTransferIsValid() throws Exception {
        TransferRequestDTO request = buildTransferRequest("bob@wonwire.com", new BigDecimal("10000"));

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fromEmail").value("alice@wonwire.com"))
                .andExpect(jsonPath("$.toEmail").value("bob@wonwire.com"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void transfer_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        TransferRequestDTO request = buildTransferRequest("bob@wonwire.com", new BigDecimal("10000"));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void transfer_ShouldReturn400_WhenSendingToSelf() throws Exception {
        TransferRequestDTO request = buildTransferRequest("alice@wonwire.com", new BigDecimal("10000"));

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_ShouldReturn422_WhenBalanceIsInsufficient() throws Exception {
        // New user with 0 balance tries to send money
        registerUser("broke@wonwire.com", "Broke", "User");
        String brokeToken = loginAndGetToken("broke@wonwire.com");

        TransferRequestDTO request = buildTransferRequest("bob@wonwire.com", new BigDecimal("99999999"));

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + brokeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void transfer_ShouldReturn201WithSameResult_WhenIdempotencyKeyIsReused() throws Exception {
        TransferRequestDTO request = buildTransferRequest("bob@wonwire.com", new BigDecimal("10000"));

        // First request
        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Same request with same idempotency key — should not double-charge
        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fromEmail").value("alice@wonwire.com"))
                .andExpect(jsonPath("$.toEmail").value("bob@wonwire.com"));
    }

    // -------------------------------------------------------------------------
    // GET /api/transfers
    // -------------------------------------------------------------------------

    @Test
    void getHistory_ShouldReturn200WithEmptyPage_WhenNoTransactions() throws Exception {
        mockMvc.perform(get("/api/transfers")
                        .header("Authorization", "Bearer " + senderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void getHistory_ShouldReturn200WithTransactions_AfterTransfer() throws Exception {
        // Make a transfer first
        TransferRequestDTO request = buildTransferRequest("bob@wonwire.com", new BigDecimal("5000"));
        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Sender sees the transaction
        mockMvc.perform(get("/api/transfers")
                        .header("Authorization", "Bearer " + senderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].fromEmail").value("alice@wonwire.com"))
                .andExpect(jsonPath("$.content[0].toEmail").value("bob@wonwire.com"));

        // Receiver also sees the transaction
        mockMvc.perform(get("/api/transfers")
                        .header("Authorization", "Bearer " + receiverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getHistory_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/transfers"))
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

        String json = result.getResponse().getContentAsString();
        return objectMapper.readTree(json).get("token").asText();
    }

    private TransferRequestDTO buildTransferRequest(String toEmail, BigDecimal amount) {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setToEmail(toEmail);
        request.setAmount(amount);
        request.setDescription("Test transfer");
        request.setIdempotencyKey(UUID.randomUUID().toString());
        return request;
    }

    private void creditWallet(String email, BigDecimal amount) {
        userRepository.findByEmail(email).ifPresent(user -> {
            Wallet wallet = walletRepository.findByUser(user).orElseThrow();
            wallet.setBalance(amount);
            walletRepository.save(wallet);
        });
    }
}