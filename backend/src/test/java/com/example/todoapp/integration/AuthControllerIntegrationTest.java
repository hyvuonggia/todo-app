package com.example.todoapp.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.example.todoapp.dto.LoginRequest;
import com.example.todoapp.dto.RegisterRequest;
import com.example.todoapp.model.User;
import com.example.todoapp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        
        // Clean up any existing users
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void registerUser_IntegrationTest() throws Exception {
        // Given
        RegisterRequest registerRequest = new RegisterRequest("integrationuser", "integration@example.com", "password123");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("integrationuser"))
                .andExpect(jsonPath("$.email").value("integration@example.com"));

        // Verify user was actually saved to database
        var savedUser = userRepository.findByUsername("integrationuser");
        assert savedUser.isPresent();
        assert !savedUser.get().getPassword().equals("password123"); // Should be encoded
    }

    @Test
    @WithMockUser
    void registerUser_DuplicateUsername_ShouldFail() throws Exception {
        // Given - First user
        RegisterRequest firstUser = new RegisterRequest("duplicateuser", "first@example.com", "password123");

        // Register first user
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isOk());

        // Given - Second user with same username
        RegisterRequest duplicateUser = new RegisterRequest("duplicateuser", "second@example.com", "password456");

        // When & Then - Should fail due to duplicate username
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void registerUser_DuplicateEmail_ShouldFail() throws Exception {
        // Given - First user
        RegisterRequest firstUser = new RegisterRequest("firstuser", "duplicate@example.com", "password123");

        // Register first user
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isOk());

        // Given - Second user with same email
        RegisterRequest duplicateEmailUser = new RegisterRequest("seconduser", "duplicate@example.com", "password456");

        // When & Then - Should fail due to duplicate email
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateEmailUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void loginUser_IntegrationTest() throws Exception {
        // Given - Register a user first
        RegisterRequest registerRequest = new RegisterRequest("loginuser", "login@example.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // When - Login with the registered user
        LoginRequest loginRequest = new LoginRequest("loginuser", "password123");

        // Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    @WithMockUser
    void loginUser_InvalidCredentials_ShouldFail() throws Exception {
        // Given - Register a user first
        RegisterRequest registerRequest = new RegisterRequest("validuser", "valid@example.com", "correctpassword");

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // When - Try to login with wrong password
        LoginRequest loginRequest = new LoginRequest("validuser", "wrongpassword");

        // Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void loginUser_NonExistentUser_ShouldFail() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("nonexistentuser", "anypassword");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
