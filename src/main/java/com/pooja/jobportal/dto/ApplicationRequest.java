package com.pooja.jobportal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request object for creating a job application")
public class ApplicationRequest {
    
    @NotNull(message = "Job ID is required")
    @Schema(description = "ID of the job being applied for", example = "1", required = true)
    private Long jobId;
    
    @NotBlank(message = "Applicant name is required")
    @Schema(description = "Full name of the applicant", example = "Jane Smith", required = true)
    private String applicantName;
    
    @NotBlank(message = "Applicant email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email address of the applicant", example = "jane.smith@email.com", required = true)
    private String applicantEmail;
    
    @Schema(description = "Phone number of the applicant", example = "+1-555-123-4567")
    private String applicantPhone;
    
    @Schema(description = "URL to the applicant's resume", example = "https://example.com/resume.pdf")
    private String resumeUrl;
    
    @Schema(description = "Cover letter from the applicant", 
            example = "I am very interested in this position because...")
    private String coverLetter;
    
    @Schema(description = "Applicant's work experience", 
            example = "5 years of software development experience...")
    private String experience;
    
    @Schema(description = "Applicant's educational background", 
            example = "Bachelor of Science in Computer Science...")
    private String education;
}