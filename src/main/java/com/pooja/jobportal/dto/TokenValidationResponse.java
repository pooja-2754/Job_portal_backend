package com.pooja.jobportal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Response object for token validation")
public class TokenValidationResponse {
    @Schema(description = "Indicates if the token is valid", example = "true")
    private boolean valid;
    
    @Schema(description = "Email associated with the token", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "Expiration time of the token in milliseconds", example = "1703020800000")
    private long expirationTime;
    
    @Schema(description = "Response message", example = "Token is valid")
    private String message;
    
    @Schema(description = "Role of the user", example = "JOB_SEEKER")
    private String role;
}