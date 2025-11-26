package com.pooja.jobportal.dto;

import com.pooja.jobportal.model.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request object for updating application status")
public class ApplicationStatusUpdateRequest {
    
    @NotNull(message = "Status is required")
    @Schema(description = "New application status", example = "UNDER_REVIEW", required = true)
    private ApplicationStatus status;
}