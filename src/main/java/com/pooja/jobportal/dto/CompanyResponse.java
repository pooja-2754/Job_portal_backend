package com.pooja.jobportal.dto;

import com.pooja.jobportal.model.CompanyVerificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload for company information")
public class CompanyResponse {

    @Schema(description = "Company ID", example = "501")
    private Long id;

    @Schema(description = "Company name", example = "Tech Corp")
    private String name;

    @Schema(description = "Company logo URL", example = "https://cdn.example.com/logos/techcorp.png")
    private String logoUrl;

    @Schema(description = "Company website", example = "https://techcorp.com")
    private String website;

    @Schema(description = "Company description")
    private String description;

    @Schema(description = "Company industry", example = "Technology")
    private String industry;

    @Schema(description = "Company size", example = "100-500")
    private String companySize;

    @Schema(description = "Verification status", example = "VERIFIED")
    private CompanyVerificationStatus verificationStatus;

    @Schema(description = "Verification timestamp")
    private LocalDateTime verifiedAt;

    @Schema(description = "Company email for authentication")
    private String email;

    @Schema(description = "Admin ID who manages this company")
    private Long adminId;

    @Schema(description = "Admin name who manages this company")
    private String adminName;

    @Schema(description = "Job count associated with this company")
    private Long jobCount;

    @Schema(description = "Company creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Company update timestamp")
    private LocalDateTime updatedAt;
}