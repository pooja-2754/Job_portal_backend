package com.pooja.jobportal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request object for token refresh")
public class RefreshTokenRequest {
    @Schema(description = "Current JWT token that needs to be refreshed", example = "eyJhbGciOiJIUzI1NiJ9...", required = true)
    private String token;
}