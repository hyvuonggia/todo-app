package com.example.todoapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.todoapp.model.User;
import com.example.todoapp.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setName("Test User");
    }

    @Test
    void registerUser_Success() {
        // Given
        User inputUser = new User();
        inputUser.setUsername("newuser");
        inputUser.setEmail("newuser@example.com");
        inputUser.setPassword("plainpassword");
        inputUser.setName("New User");

        String encodedPassword = "encodedPassword123";
        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("newuser");
        savedUser.setEmail("newuser@example.com");
        savedUser.setPassword(encodedPassword);
        savedUser.setName("New User");
        savedUser.setProvider("local");

        when(passwordEncoder.encode("plainpassword")).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userService.registerUser(inputUser);

        // Then
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("newuser@example.com", result.getEmail());
        assertEquals(encodedPassword, result.getPassword());
        assertEquals("local", result.getProvider());
        assertEquals(2L, result.getId());

        verify(passwordEncoder).encode("plainpassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_NullInput() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            userService.registerUser(null);
        });

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_EmptyPassword() {
        // Given
        User inputUser = new User();
        inputUser.setUsername("newuser");
        inputUser.setEmail("newuser@example.com");
        inputUser.setPassword("");
        inputUser.setName("New User");

        String encodedPassword = "encoded";
        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("newuser");
        savedUser.setEmail("newuser@example.com");
        savedUser.setPassword(encodedPassword);
        savedUser.setName("New User");
        savedUser.setProvider("local");

        when(passwordEncoder.encode("")).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userService.registerUser(inputUser);

        // Then
        assertNotNull(result);
        assertEquals(encodedPassword, result.getPassword());
        assertEquals("local", result.getProvider());

        verify(passwordEncoder).encode("");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_DatabaseError() {
        // Given
        User inputUser = new User();
        inputUser.setUsername("newuser");
        inputUser.setEmail("newuser@example.com");
        inputUser.setPassword("plainpassword");
        inputUser.setName("New User");

        when(passwordEncoder.encode("plainpassword")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.registerUser(inputUser);
        });

        verify(passwordEncoder).encode("plainpassword");
        verify(userRepository).save(any(User.class));
    }
}
