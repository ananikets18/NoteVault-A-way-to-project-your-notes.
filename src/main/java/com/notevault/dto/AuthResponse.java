package com.notevault.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object returned upon successful authentication.
 */
@Schema(description = "Authentication response containing JWT token")
public class AuthResponse {

    @Schema(description = "JWT Bearer token")
    private String token;

    @Schema(description = "Token type prefix", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Authenticated user ID", example = "1")
    private Long userId;

    @Schema(description = "User's full name", example = "John Doe")
    private String name;

    @Schema(description = "User's email", example = "john@example.com")
    private String email;

    public AuthResponse() {
    }

    public AuthResponse(String token, Long userId, String name, String email) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.tokenType = "Bearer";
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
