package com.wonwire.wonwire.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonwire.wonwire.annotation.IntegrationTest;
import com.wonwire.wonwire.dto.LoginRequestDTO;
import com.wonwire.wonwire.dto.RegisterRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class UserControllerTest {

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
    // GET /api/user/profile
    // -------------------------------------------------------------------------

    @Test
    void getProfile_ShouldReturn200WithUserInfo_WhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/user/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email").value("alice@wonwire.com"));
    }

    @Test
    void getProfile_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // PUT /api/user/profile
    // -------------------------------------------------------------------------

    @Test
    void updateProfile_ShouldReturn200WithUpdatedInfo_WhenRequestIsValid() throws Exception {
        mockMvc.perform(put("/api/user/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "Alicia",
                                    "lastName": "Johnson",
                                    "email": "alice@wonwire.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alicia"))
                .andExpect(jsonPath("$.lastName").value("Johnson"))
                .andExpect(jsonPath("$.email").value("alice@wonwire.com"));
    }

    @Test
    void updateProfile_ShouldReturn409_WhenEmailAlreadyTaken() throws Exception {
        // Register a second user
        registerUser("bob@wonwire.com", "Bob", "Martin");

        // Alice tries to take Bob's email
        mockMvc.perform(put("/api/user/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "Alice",
                                    "lastName": "Smith",
                                    "email": "bob@wonwire.com"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void updateProfile_ShouldReturn400_WhenEmailIsInvalid() throws Exception {
        mockMvc.perform(put("/api/user/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "Alice",
                                    "lastName": "Smith",
                                    "email": "not-an-email"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProfile_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "Alice",
                                    "lastName": "Smith",
                                    "email": "alice@wonwire.com"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // POST /api/user/change-password
    // -------------------------------------------------------------------------

    @Test
    void changePassword_ShouldReturn200_WhenCurrentPasswordIsCorrect() throws Exception {
        mockMvc.perform(post("/api/user/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "currentPassword": "password123",
                                    "newPassword": "newPassword123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password successfully updated"));
    }

    @Test
    void changePassword_ShouldReturn401_WhenCurrentPasswordIsWrong() throws Exception {
        mockMvc.perform(post("/api/user/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "currentPassword": "wrongPassword",
                                    "newPassword": "newPassword123"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_ShouldReturn400_WhenNewPasswordIsTooShort() throws Exception {
        mockMvc.perform(post("/api/user/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "currentPassword": "password123",
                                    "newPassword": "short"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "currentPassword": "password123",
                                    "newPassword": "newPassword123"
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