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
        User newUser = new User();
        newUser.setUsername("integrationuser");
        newUser.setEmail("integration@example.com");
        newUser.setPassword("password123");
        newUser.setName("Integration User");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("integrationuser"))
                .andExpect(jsonPath("$.email").value("integration@example.com"))
                .andExpect(jsonPath("$.name").value("Integration User"))
                .andExpect(jsonPath("$.provider").value("local"))
                .andExpect(jsonPath("$.password").exists()); // Password should be encoded

        // Verify user was actually saved to database
        var savedUser = userRepository.findByUsername("integrationuser");
        assert savedUser.isPresent();
        assert !savedUser.get().getPassword().equals("password123"); // Should be encoded
    }

    @Test
    @WithMockUser
    void registerUser_DuplicateUsername_ShouldFail() throws Exception {
        // Given - First user
        User firstUser = new User();
        firstUser.setUsername("duplicateuser");
        firstUser.setEmail("first@example.com");
        firstUser.setPassword("password123");
        firstUser.setName("First User");

        // Register first user
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isOk());

        // Given - Second user with same username
        User duplicateUser = new User();
        duplicateUser.setUsername("duplicateuser"); // Same username
        duplicateUser.setEmail("second@example.com");
        duplicateUser.setPassword("password456");
        duplicateUser.setName("Second User");

        // When & Then - Should fail due to duplicate username
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void registerUser_DuplicateEmail_ShouldFail() throws Exception {
        // Given - First user
        User firstUser = new User();
        firstUser.setUsername("firstuser");
        firstUser.setEmail("duplicate@example.com");
        firstUser.setPassword("password123");
        firstUser.setName("First User");

        // Register first user
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isOk());

        // Given - Second user with same email
        User duplicateEmailUser = new User();
        duplicateEmailUser.setUsername("seconduser");
        duplicateEmailUser.setEmail("duplicate@example.com"); // Same email
        duplicateEmailUser.setPassword("password456");
        duplicateEmailUser.setName("Second User");

        // When & Then - Should fail due to duplicate email
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateEmailUser)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void loginUser_IntegrationTest() throws Exception {
        // Given - Register a user first
        User newUser = new User();
        newUser.setUsername("loginuser");
        newUser.setEmail("login@example.com");
        newUser.setPassword("password123");
        newUser.setName("Login User");

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk());

        // When - Login with the registered user
        User loginRequest = new User();
        loginRequest.setUsername("loginuser");
        loginRequest.setPassword("password123");

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
        User newUser = new User();
        newUser.setUsername("validuser");
        newUser.setEmail("valid@example.com");
        newUser.setPassword("correctpassword");
        newUser.setName("Valid User");

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk());

        // When - Try to login with wrong password
        User loginRequest = new User();
        loginRequest.setUsername("validuser");
        loginRequest.setPassword("wrongpassword");

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
        User loginRequest = new User();
        loginRequest.setUsername("nonexistentuser");
        loginRequest.setPassword("anypassword");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
