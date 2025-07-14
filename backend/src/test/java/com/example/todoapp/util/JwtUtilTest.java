package com.example.todoapp.util;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private UserDetails userDetails;
    private String validToken;

    @BeforeEach
    void setUp() {
        // Set up the JWT configuration
        ReflectionTestUtils.setField(jwtUtil, "secret", "testSecretKeyForJWTTokenGenerationThatIsLongEnoughForHS512Algorithm");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L); // 1 hour

        userDetails = new User("testuser", "password", new ArrayList<>());
        validToken = jwtUtil.generateToken(userDetails);
    }

    @Test
    void generateToken_Success() {
        // When
        String token = jwtUtil.generateToken(userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains(".")); // JWT format has dots
        
        // Verify the token parts (header.payload.signature)
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    void extractUsername_Success() {
        // When
        String username = jwtUtil.extractUsername(validToken);

        // Then
        assertEquals("testuser", username);
    }

    @Test
    void extractExpiration_Success() {
        // When
        Date expiration = jwtUtil.extractExpiration(validToken);

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date())); // Should be in the future
        
        // Should be approximately 1 hour from now
        long timeDifference = expiration.getTime() - System.currentTimeMillis();
        assertTrue(timeDifference > 3590000); // More than 59 minutes 50 seconds
        assertTrue(timeDifference < 3610000); // Less than 60 minutes 10 seconds
    }

    @Test
    void validateToken_ValidToken() {
        // When
        Boolean isValid = jwtUtil.validateToken(validToken, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_InvalidUsername() {
        // Given
        UserDetails differentUser = new User("differentuser", "password", new ArrayList<>());

        // When
        Boolean isValid = jwtUtil.validateToken(validToken, differentUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_ExpiredToken() {
        // Given - Create a token with very short expiration
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L); // 1 millisecond
        String expiredToken = jwtUtil.generateToken(userDetails);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Reset expiration for validation
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L);

        // When & Then - extractExpiration should throw ExpiredJwtException
        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtil.extractExpiration(expiredToken);
        });
    }

    @Test
    void extractUsername_InvalidToken() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        assertThrows(MalformedJwtException.class, () -> {
            jwtUtil.extractUsername(invalidToken);
        });
    }

    @Test
    void extractUsername_MalformedToken() {
        // Given
        String malformedToken = "not-a-jwt-token";

        // When & Then
        assertThrows(MalformedJwtException.class, () -> {
            jwtUtil.extractUsername(malformedToken);
        });
    }

    @Test
    void extractUsername_EmptyToken() {
        // Given
        String emptyToken = "";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.extractUsername(emptyToken);
        });
    }

    @Test
    void extractUsername_NullToken() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.extractUsername(null);
        });
    }

    @Test
    void validateToken_SignatureException() {
        // Given - Create token with different secret (longer to meet minimum requirements)
        ReflectionTestUtils.setField(jwtUtil, "secret", "differentSecretKeyForJWTTokenGenerationThatIsLongEnoughForHS512AlgorithmWith256Bits");
        String tokenWithDifferentSecret = jwtUtil.generateToken(userDetails);
        
        // Change secret back
        ReflectionTestUtils.setField(jwtUtil, "secret", "testSecretKeyForJWTTokenGenerationThatIsLongEnoughForHS512Algorithm");

        // When & Then
        assertThrows(SignatureException.class, () -> {
            jwtUtil.validateToken(tokenWithDifferentSecret, userDetails);
        });
    }

    @Test
    void extractClaim_Success() {
        // When
        String subject = jwtUtil.extractClaim(validToken, Claims::getSubject);
        Date issuedAt = jwtUtil.extractClaim(validToken, Claims::getIssuedAt);

        // Then
        assertEquals("testuser", subject);
        assertNotNull(issuedAt);
        assertTrue(issuedAt.before(new Date()) || issuedAt.equals(new Date()));
    }

    @Test
    void generateToken_DifferentUsers() {
        // Given
        UserDetails user1 = new User("user1", "password1", new ArrayList<>());
        UserDetails user2 = new User("user2", "password2", new ArrayList<>());

        // When
        String token1 = jwtUtil.generateToken(user1);
        String token2 = jwtUtil.generateToken(user2);

        // Then
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
        
        assertEquals("user1", jwtUtil.extractUsername(token1));
        assertEquals("user2", jwtUtil.extractUsername(token2));
    }

    @Test
    void generateToken_SameUserMultipleTimes() throws InterruptedException {
        // When
        String token1 = jwtUtil.generateToken(userDetails);
        Thread.sleep(1000); // Wait 1 second to ensure different timestamps
        String token2 = jwtUtil.generateToken(userDetails);

        // Then
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2); // Should be different due to different issued times
        
        assertEquals("testuser", jwtUtil.extractUsername(token1));
        assertEquals("testuser", jwtUtil.extractUsername(token2));
    }

    @Test
    void extractAllClaims_Success() {
        // This is testing the private method indirectly through public methods
        
        // When
        String username = jwtUtil.extractUsername(validToken);
        Date expiration = jwtUtil.extractExpiration(validToken);

        // Then
        assertNotNull(username);
        assertNotNull(expiration);
        assertEquals("testuser", username);
    }
}
