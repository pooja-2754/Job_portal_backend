package com.pooja.jobportal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response object for authentication operations")
public class AuthResponse {
    @Schema(description = "JWT authentication token (null for signup)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;
    
    @Schema(description = "User information (included on login)")
    private UserResponse user;
    
    @Schema(description = "Company information (included on company login)")
    private CompanyResponse company;
    
    @Schema(description = "Response message", example = "Login successful")
    private String message;

    // Static factory methods to avoid constructor ambiguity
    
    public static AuthResponse withUser(String token, UserResponse user, String message) {
        AuthResponse response = new AuthResponse();
        response.token = token;
        response.user = user;
        response.company = null;
        response.message = message;
        return response;
    }

    public static AuthResponse withCompany(String token, CompanyResponse company, String message) {
        AuthResponse response = new AuthResponse();
        response.token = token;
        response.user = null;
        response.company = company;
        response.message = message;
        return response;
    }

    public static AuthResponse messageOnly(String message) {
        AuthResponse response = new AuthResponse();
        response.token = null;
        response.user = null;
        response.company = null;
        response.message = message;
        return response;
    }
}
