package com.example.todoapp.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.todoapp.service.CustomUserDetailsService;
import com.example.todoapp.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT authentication filter that processes JWT tokens for each incoming request.
 * This filter extends OncePerRequestFilter to ensure it's executed once per request.
 * It extracts JWT tokens from the Authorization header, validates them, and sets up
 * the security context for authenticated users.
 *
 * @author Todo App Team
 * @version 1.0
 * @since 2025-01-01
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;

    /**
     * Constructor for JwtRequestFilter.
     * Initializes the filter with required dependencies for JWT processing.
     *
     * @param customUserDetailsService service for loading user details by username
     * @param jwtUtil utility for JWT token operations (validation, extraction, etc.)
     */
    public JwtRequestFilter(CustomUserDetailsService customUserDetailsService, JwtUtil jwtUtil) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Processes each incoming HTTP request to check for valid JWT tokens.
     * This method:
     * 1. Extracts the JWT token from the Authorization header
     * 2. Validates the token and extracts the username
     * 3. Loads user details and sets up the security context if valid
     * 4. Continues the filter chain
     *
     * @param request the HttpServletRequest being processed
     * @param response the HttpServletResponse for the request
     * @param chain the FilterChain to continue processing
     * @throws ServletException if a servlet-related error occurs
     * @throws IOException if an I/O error occurs during request processing
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }
}