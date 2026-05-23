package com.pooja.jobportal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request object for user login")
public class LoginRequest {
    @Schema(description = "Email address of the user", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
    
    @Schema(description = "Password for the user account", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
