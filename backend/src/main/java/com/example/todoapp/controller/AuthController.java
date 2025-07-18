package com.example.todoapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.example.todoapp.model.JwtResponse;
import com.example.todoapp.model.User;
import com.example.todoapp.service.CustomUserDetailsService;
import com.example.todoapp.service.UserService;
import com.example.todoapp.util.JwtUtil;

/**
 * REST controller for authentication-related endpoints.
 * Handles user registration and login operations.
 * 
 * @author Todo App Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Registers a new user in the system.
     * 
     * @param user the user registration information
     * @return ResponseEntity containing the registered user information
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            return ResponseEntity.ok(userService.registerUser(user));
        } catch (DataIntegrityViolationException e) {
            String message = e.getMessage();
            if (message.contains("email")) {
                return ResponseEntity.badRequest().body("Email already exists");
            } else if (message.contains("username")) {
                return ResponseEntity.badRequest().body("Username already exists");
            } else {
                return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
            }
        }
    }

    /**
     * Authenticates a user and returns a JWT token.
     * 
     * @param user the user login credentials (username and password)
     * @return ResponseEntity containing a JWT token if authentication is successful
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are invalid
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        // Check for null or empty username/password
        if (user.getUsername() == null || user.getUsername().trim().isEmpty() ||
            user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username and password are required");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            final String token = jwtUtil.generateToken(userDetails);
            
            // Get the full user information
            User authenticatedUser = userService.findByUsername(user.getUsername());

            return ResponseEntity.ok(new JwtResponse(token, authenticatedUser.getUsername(), authenticatedUser.getEmail()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}
