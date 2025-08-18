package com.example.task.dto;

public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";

    public LoginResponse(String token) { this.token = token; }

    // Getter only
    public String getToken() { return token; }
    public String getTokenType() { return tokenType; }
}
