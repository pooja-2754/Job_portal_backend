package com.pooja.jobportal.dto;

import com.pooja.jobportal.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request object for user registration")
public class SignupRequest {
    @Schema(description = "Full name of the user", example = "John Doe", required = true)
    private String name;
    
    @Schema(description = "Email address of the user", example = "john.doe@example.com", required = true)
    private String email;
    
    @Schema(description = "Password for the user account", example = "password123", required = true)
    private String password;
    
    @Schema(description = "Role of the user in the system", example = "JOB_SEEKER", required = false)
    private Role role;
    
    @Schema(description = "Skills of the user (for job seekers)", example = "Java, Spring Boot, React")
    private String skills;
}
