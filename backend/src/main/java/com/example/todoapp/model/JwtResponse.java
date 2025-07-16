package com.example.todoapp.model;

import java.io.Serializable;

/**
 * Response model for JWT authentication.
 * This class encapsulates the JWT token returned after successful authentication.
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
     * Constructor to create a JWT response with the provided token.
     * 
     * @param jwttoken the JWT token string to be included in the response
     */
    public JwtResponse(String jwttoken) {
        this.jwttoken = jwttoken;
    }

    /**
     * Gets the JWT token from the response.
     * 
     * @return the JWT token string
     */
    public String getToken() {
        return this.jwttoken;
    }
}
