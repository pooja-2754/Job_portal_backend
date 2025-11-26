package com.pooja.jobportal.dto;

import com.pooja.jobportal.model.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "Response object for application information")
public class ApplicationResponse {
    
    @Schema(description = "Unique application identifier", example = "1")
    private Long id;
    
    @Schema(description = "ID of the job applied for", example = "5")
    private Long jobId;
    
    @Schema(description = "Title of the job applied for", example = "Senior Java Developer")
    private String jobTitle;
    
    @Schema(description = "Company offering the job", example = "Tech Corp")
    private String jobCompany;
    
    @Schema(description = "Name of the applicant", example = "Jane Smith")
    private String applicantName;
    
    @Schema(description = "Email of the applicant", example = "jane.smith@email.com")
    private String applicantEmail;
    
    @Schema(description = "Phone number of the applicant", example = "+1-555-123-4567")
    private String applicantPhone;
    
    @Schema(description = "URL to the applicant's resume", example = "https://example.com/resume.pdf")
    private String resumeUrl;
    
    @Schema(description = "Cover letter from the applicant")
    private String coverLetter;
    
    @Schema(description = "Applicant's work experience")
    private String experience;
    
    @Schema(description = "Applicant's educational background")
    private String education;
    
    @Schema(description = "Current application status", example = "PENDING")
    private ApplicationStatus status;
    
    @Schema(description = "Display name for application status", example = "Pending")
    private String statusDisplayName;
    
    @Schema(description = "Date when the application was submitted", example = "2024-01-15T10:30:00")
    private LocalDateTime appliedDate;
    
    @Schema(description = "Date when the application was last updated", example = "2024-01-20T14:22:00")
    private LocalDateTime updatedAt;
}