package com.pooja.jobportal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "Response object for resume data with primary status")
public class ResumeResponse {
    
    @Schema(description = "Resume ID", example = "1")
    private Long id;
    
    @Schema(description = "Resume name", example = "John_Doe_Resume.pdf")
    private String name;
    
    @Schema(description = "Resume file URL", example = "https://cloudinary.com/resume/abc123.pdf")
    private String fileUrl;
    
    @Schema(description = "Resume preview URL", example = "https://cloudinary.com/resume/abc123.jpg")
    private String previewUrl;
    
    @Schema(description = "Cloudinary ID for file management", example = "resume_abc123")
    private String cloudinaryId;
    
    @Schema(description = "Upload timestamp", example = "2023-12-01T15:20:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Whether this resume is set as primary", example = "true")
    @JsonProperty("isPrimary")  // Ensure JSON output uses isPrimary
    private boolean isPrimary;
    
    @Schema(description = "Whether this resume is set as primary", example = "true")
    @JsonProperty("primary")  // Alternative field name for frontend compatibility
    private boolean isPrimaryAlternative;
    
    // Custom getter to support both field names
    public boolean getPrimary() {
        return isPrimary;
    }
}