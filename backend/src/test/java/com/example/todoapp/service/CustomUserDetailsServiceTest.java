package com.example.todoapp.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.todoapp.model.User;
import com.example.todoapp.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword123");
        testUser.setName("Test User");
        testUser.setProvider("local");
    }

    @Test
    void loadUserByUsername_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        // Then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword123", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty()); // No authorities assigned
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("nonexistentuser");
        });

        assertEquals("User not found with username: nonexistentuser", exception.getMessage());
        verify(userRepository).findByUsername("nonexistentuser");
    }

    @Test
    void loadUserByUsername_NullUsername() {
        // Given
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(null);
        });

        assertEquals("User not found with username: null", exception.getMessage());
        verify(userRepository).findByUsername(null);
    }

    @Test
    void loadUserByUsername_EmptyUsername() {
        // Given
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("");
        });

        assertEquals("User not found with username: ", exception.getMessage());
        verify(userRepository).findByUsername("");
    }

    @Test
    void loadUserByUsername_CaseInsensitive() {
        // Given
        when(userRepository.findByUsername("TESTUSER")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("TESTUSER");
        });

        assertEquals("User not found with username: TESTUSER", exception.getMessage());
        verify(userRepository).findByUsername("TESTUSER");
        
        // Note: The actual case sensitivity depends on how the repository is implemented
        // This test assumes case-sensitive username lookup
    }

    @Test
    void loadUserByUsername_RepositoryThrowsException() {
        // Given
        when(userRepository.findByUsername("testuser")).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            customUserDetailsService.loadUserByUsername("testuser");
        });

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_UserWithNullPassword() {
        // Given
        testUser.setPassword(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        // Then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_UserWithEmptyPassword() {
        // Given
        testUser.setPassword("");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        // Then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());

        verify(userRepository).findByUsername("testuser");
    }
}
