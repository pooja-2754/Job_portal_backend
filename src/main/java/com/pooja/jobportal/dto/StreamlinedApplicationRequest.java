package com.pooja.jobportal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request object for creating a job application for authenticated candidates")
public class StreamlinedApplicationRequest {
    
    @NotNull(message = "Job ID is required")
    @Schema(description = "ID of the job being applied for", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long jobId;
    
    @Schema(description = "Cover letter from the applicant", 
            example = "I am very interested in this position because...")
    private String coverLetter;
    
    @Schema(description = "Custom resume URL specific to this application (overrides profile resume)", 
            example = "https://example.com/custom-resume.pdf")
    private String customResumeUrl;
}