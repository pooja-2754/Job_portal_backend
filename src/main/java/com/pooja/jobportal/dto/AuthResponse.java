package com.pooja.jobportal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Response object for authentication operations")
public class AuthResponse {
    @Schema(description = "JWT authentication token (null for signup)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;
    
    @Schema(description = "User information (included on login)")
    private UserResponse user;
    
    @Schema(description = "Response message", example = "Login successful")
    private String message;
}
