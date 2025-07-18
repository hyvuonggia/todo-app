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

import com.example.todoapp.dto.LoginRequest;
import com.example.todoapp.dto.RegisterRequest;
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
     * Registers a new user in the system and automatically logs them in.
     * 
     * @param registerRequest the user registration information
     * @return ResponseEntity containing JWT token and user information
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Create User entity from the request
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(registerRequest.getPassword());
            
            // Store the original password for authentication
            String originalPassword = registerRequest.getPassword();
            
            // Register the new user (this will encode the password)
            User registeredUser = userService.registerUser(user);
            
            // Automatically authenticate the user with the original password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(registerRequest.getUsername(), originalPassword));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            final UserDetails userDetails = userDetailsService.loadUserByUsername(registerRequest.getUsername());
            final String token = jwtUtil.generateToken(userDetails);

            // Return the same response format as login
            return ResponseEntity.ok(new JwtResponse(token, registeredUser.getUsername(), registeredUser.getEmail()));
            
        } catch (DataIntegrityViolationException e) {
            String message = e.getMessage();
            if (message.contains("email")) {
                return ResponseEntity.badRequest().body("Email already exists");
            } else if (message.contains("username")) {
                return ResponseEntity.badRequest().body("Username already exists");
            } else {
                return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Authenticates a user and returns a JWT token.
     * 
     * @param loginRequest the user login credentials (username and password)
     * @return ResponseEntity containing a JWT token if authentication is successful
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are invalid
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            final String token = jwtUtil.generateToken(userDetails);
            
            // Get the full user information
            User authenticatedUser = userService.findByUsername(loginRequest.getUsername());

            return ResponseEntity.ok(new JwtResponse(token, authenticatedUser.getUsername(), authenticatedUser.getEmail()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}
