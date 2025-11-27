package com.pooja.jobportal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating or updating a company")
public class CompanyRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    @Schema(description = "Company name", example = "Tech Corp", required = true)
    private String name;

    @Schema(description = "Company logo URL", example = "https://cdn.example.com/logos/techcorp.png")
    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;

    @Schema(description = "Company website", example = "https://techcorp.com")
    @Size(max = 500, message = "Website URL must not exceed 500 characters")
    private String website;

    @Schema(description = "Company description")
    private String description;

    @Schema(description = "Company industry", example = "Technology")
    @Size(max = 100, message = "Industry must not exceed 100 characters")
    private String industry;

    @Schema(description = "Company size", example = "100-500")
    @Size(max = 50, message = "Company size must not exceed 50 characters")
    private String companySize;
}