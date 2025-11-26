package com.pooja.jobportal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "User information response")
public class UserResponse {
    @Schema(description = "User ID", example = "123")
    private Long id;
    
    @Schema(description = "User email", example = "amit.k@one.com")
    private String email;
    
    @Schema(description = "User name", example = "Amit K")
    private String name;
    
    @Schema(description = "User role", example = "JOB_SEEKER")
    private String role;
    
    @Schema(description = "User skills", example = "Java, Spring Boot, React")
    private String skills;
}