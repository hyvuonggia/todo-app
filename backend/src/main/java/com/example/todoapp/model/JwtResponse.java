package com.example.todoapp.model;

import java.io.Serializable;

/**
 * Response model for JWT authentication.
 * This class encapsulates the JWT token and user information returned after successful authentication.
 * 
 * @author Todo App Team
 * @version 1.0
 * @since 1.0
 */
public class JwtResponse implements Serializable {

    private static final long serialVersionUID = -8091879091924046844L;
    
    /**
     * The JWT token string.
     */
    private final String jwttoken;
    
    /**
     * The username of the authenticated user.
     */
    private final String username;
    
    /**
     * The email of the authenticated user.
     */
    private final String email;

    /**
     * Constructor to create a JWT response with the provided token and user information.
     * 
     * @param jwttoken the JWT token string to be included in the response
     * @param username the username of the authenticated user
     * @param email the email of the authenticated user
     */
    public JwtResponse(String jwttoken, String username, String email) {
        this.jwttoken = jwttoken;
        this.username = username;
        this.email = email;
    }

    /**
     * Gets the JWT token from the response.
     * 
     * @return the JWT token string
     */
    public String getToken() {
        return this.jwttoken;
    }
    
    /**
     * Gets the username from the response.
     * 
     * @return the username string
     */
    public String getUsername() {
        return this.username;
    }
    
    /**
     * Gets the email from the response.
     * 
     * @return the email string
     */
    public String getEmail() {
        return this.email;
    }
}
