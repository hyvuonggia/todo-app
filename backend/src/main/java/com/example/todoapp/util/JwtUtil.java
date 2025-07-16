package com.example.todoapp.util;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Utility class for JSON Web Token (JWT) operations.
 * This class provides methods for generating, parsing, validating, and extracting
 * information from JWT tokens used for authentication and authorization.
 *
 * @author Todo App Team
 * @version 1.0
 * @since 2025-01-01
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Extracts the username (subject) from the JWT token.
     *
     * @param token the JWT token from which to extract the username
     * @return the username stored in the token's subject claim
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from the JWT token.
     *
     * @param token the JWT token from which to extract the expiration date
     * @return the Date when the token expires
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from the JWT token using a claims resolver function.
     * This is a generic method that can extract any claim from the token.
     *
     * @param <T> the type of the claim to extract
     * @param token the JWT token from which to extract the claim
     * @param claimsResolver a function that extracts the desired claim from the Claims object
     * @return the extracted claim of type T
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from the JWT token.
     * This method parses the token and returns the complete Claims object.
     *
     * @param token the JWT token to parse
     * @return the Claims object containing all token claims
     * @throws io.jsonwebtoken.JwtException if the token is invalid or malformed
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
    }

    /**
     * Checks if the JWT token has expired.
     * Compares the token's expiration date with the current date.
     *
     * @param token the JWT token to check for expiration
     * @return true if the token has expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Generates a new JWT token for the given user details.
     * Creates a token with the username as the subject and default claims.
     *
     * @param userDetails the UserDetails object containing user information
     * @return a new JWT token string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Creates a JWT token with the specified claims and username.
     * Sets the subject, issued date, expiration date, and signs the token.
     *
     * @param claims a Map of custom claims to include in the token
     * @param userName the username to set as the token's subject
     * @return a signed JWT token string
     */
    private String createToken(Map<String, Object> claims, String userName) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }

    /**
     * Validates a JWT token against the provided user details.
     * Checks if the token's username matches the user's username and if the token hasn't expired.
     *
     * @param token the JWT token to validate
     * @param userDetails the UserDetails to validate against
     * @return true if the token is valid for the given user, false otherwise
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Gets the signing key used for JWT token creation and validation.
     * Decodes the base64-encoded secret and creates an HMAC key for signing.
     *
     * @return the Key object used for signing JWT tokens
     */
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}