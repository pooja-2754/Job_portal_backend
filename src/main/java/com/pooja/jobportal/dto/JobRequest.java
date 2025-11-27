package com.pooja.jobportal.dto;

import com.pooja.jobportal.model.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "Request object for creating or updating a job")
public class JobRequest {
    
    @Schema(description = "Unique job identifier for updates", example = "1")
    private Long id;
    
    @NotBlank(message = "Job title is required")
    @Schema(description = "Title of the job position", example = "Senior Java Developer", required = true)
    private String title;
    
    @Schema(description = "Company ID if referencing an existing company", example = "501")
    private Long companyId;
    
    @Schema(description = "Company name for creating a new company", example = "Tech Corp")
    private String companyName;
    
    @Schema(description = "Company website", example = "https://techcorp.com")
    private String companyWebsite;
    
    @Schema(description = "Company logo URL", example = "https://cdn.example.com/logos/techcorp.png")
    private String companyLogoUrl;
    
    @Schema(description = "Company description")
    private String companyDescription;
    
    @Schema(description = "Company industry", example = "Technology")
    private String companyIndustry;
    
    @Schema(description = "Company size", example = "100-500")
    private String companySize;
    
    @NotNull(message = "Job status is required")
    @Schema(description = "Job publication status", example = "PUBLISHED", required = true)
    private JobStatus status = JobStatus.PUBLISHED;
    
    @NotNull(message = "Job type is required")
    @Schema(description = "Type of employment", example = "FULL_TIME", required = true)
    private JobType type;
    
    @NotNull(message = "Workplace type is required")
    @Schema(description = "Work arrangement type", example = "HYBRID", required = true)
    private WorkplaceType workplaceType = WorkplaceType.ONSITE;
    
    @Schema(description = "Experience level required", example = "SENIOR")
    private ExperienceLevel experienceLevel;
    
    @Schema(description = "Salary information")
    private SalaryRequest salary;
    
    @Schema(description = "Location information")
    private LocationRequest location;
    
    @Schema(description = "Skills required for the job", example = "[\"Java\", \"Spring Boot\", \"Microservices\", \"AWS\"]")
    private List<String> skills;
    
    @Schema(description = "External application URL", example = "https://techcorp.com/careers/apply/123")
    private String applyUrl;
    
    // Rich text fields
    @NotBlank(message = "Job description is required")
    @Schema(description = "Detailed description of the job role (HTML supported)",
            example = "<p>We are looking for a <b>passionate</b> developer...</p>", required = true)
    private String description;
    
    @Schema(description = "Job responsibilities (HTML supported)",
            example = "<ul><li>Design microservices</li><li>Lead code reviews</li></ul>")
    private String responsibilities;
    
    @Schema(description = "Required qualifications and skills (HTML supported)",
            example = "<ul><li>5+ years Java experience</li><li>Spring Boot mastery</li></ul>")
    private String requirements;
    
    @Schema(description = "Benefits and perks (HTML supported)",
            example = "<ul><li>Remote work</li><li>Health insurance</li></ul>")
    private String benefits;
    
    @Schema(description = "Application deadline", example = "2024-12-31")
    private LocalDate deadline;
    
    @Schema(description = "Whether the job is currently active", example = "true")
    private Boolean isActive = true;
    
    // Nested DTOs for structured data
    @Data
    @Schema(description = "Salary information")
    public static class SalaryRequest {
        @Schema(description = "Minimum salary", example = "80000")
        private Double min;
        
        @Schema(description = "Maximum salary", example = "120000")
        private Double max;
        
        @Schema(description = "Currency", example = "USD")
        private String currency = "USD";
        
        @Schema(description = "Salary period", example = "YEARLY")
        private Salary.SalaryPeriod period = Salary.SalaryPeriod.YEARLY;
        
        @Schema(description = "Whether salary is negotiable", example = "true")
        private Boolean isNegotiable = false;
    }
    
    @Data
    @Schema(description = "Location information")
    public static class LocationRequest {
        @Schema(description = "City", example = "New York")
        private String city;
        
        @Schema(description = "State", example = "NY")
        private String state;
        
        @Schema(description = "Country", example = "USA")
        private String country;
        
        @Schema(description = "ZIP code", example = "10001")
        private String zipCode;
        
        @Schema(description = "Coordinates")
        private CoordinatesRequest coordinates;
    }
    
    @Data
    @Schema(description = "Coordinates")
    public static class CoordinatesRequest {
        @Schema(description = "Latitude", example = "40.7128")
        private Double lat;
        
        @Schema(description = "Longitude", example = "-74.0060")
        private Double lng;
    }
}