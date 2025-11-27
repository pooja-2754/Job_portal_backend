package com.pooja.jobportal.dto;

import com.pooja.jobportal.model.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "Response object for job information")
public class JobResponse {
    
    @Schema(description = "Unique job identifier", example = "1")
    private Long id;
    
    @Schema(description = "SEO-friendly URL slug", example = "senior-java-developer-tech-corp-ny")
    private String slug;
    
    @Schema(description = "ID of the recruiter who posted this job", example = "5")
    private Long recruiterId;
    
    @Schema(description = "Name of the recruiter who posted this job", example = "John Doe")
    private String recruiterName;
    
    @Schema(description = "Email of the recruiter", example = "john.doe@company.com")
    private String recruiterEmail;
    
    @Schema(description = "Title of the job position", example = "Senior Java Developer")
    private String title;
    
    @Schema(description = "Job publication status", example = "PUBLISHED")
    private JobStatus status;
    
    @Schema(description = "Company information")
    private CompanyResponse company;
    
    @Schema(description = "Location information")
    private LocationResponse location;
    
    @Schema(description = "Type of employment", example = "FULL_TIME")
    private JobType type;
    
    @Schema(description = "Display name for job type", example = "Full-Time")
    private String typeDisplayName;
    
    @Schema(description = "Work arrangement type", example = "HYBRID")
    private WorkplaceType workplaceType;
    
    @Schema(description = "Experience level required", example = "SENIOR")
    private ExperienceLevel experienceLevel;
    
    @Schema(description = "Salary information")
    private SalaryResponse salary;
    
    @Schema(description = "Skills required for the job")
    private List<String> skills;
    
    @Schema(description = "Number of times this job was viewed", example = "340")
    private Long viewCount;
    
    @Schema(description = "External application URL", example = "https://techcorp.com/careers/apply/123")
    private String applyUrl;
    
    @Schema(description = "Detailed description of the job role (HTML)")
    private String description;
    
    @Schema(description = "Job responsibilities (HTML)")
    private String responsibilities;
    
    @Schema(description = "Required qualifications and skills (HTML)")
    private String requirements;
    
    @Schema(description = "Benefits and perks (HTML)")
    private String benefits;
    
    @Schema(description = "Date when the job was posted", example = "2024-01-15T10:30:00")
    private LocalDateTime postedDate;
    
    @Schema(description = "Application deadline", example = "2024-12-31")
    private LocalDate deadline;
    
    @Schema(description = "Days remaining until deadline", example = "15")
    private Long daysUntilDeadline;
    
    @Schema(description = "Whether the job is currently active", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Number of applications received for this job", example = "15")
    private Long applicationCount;
    
    @Schema(description = "Date when the job was created", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Date when the job was last updated", example = "2024-01-20T14:22:00")
    private LocalDateTime updatedAt;
    
    // Nested response classes
    @Data
    @Builder
    @AllArgsConstructor
    @Schema(description = "Company information")
    public static class CompanyResponse {
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
        private String size;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @Schema(description = "Location information")
    public static class LocationResponse {
        @Schema(description = "City", example = "New York")
        private String city;
        
        @Schema(description = "State", example = "NY")
        private String state;
        
        @Schema(description = "Country", example = "USA")
        private String country;
        
        @Schema(description = "ZIP code", example = "10001")
        private String zipCode;
        
        @Schema(description = "Coordinates")
        private CoordinatesResponse coordinates;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @Schema(description = "Coordinates")
    public static class CoordinatesResponse {
        @Schema(description = "Latitude", example = "40.7128")
        private Double lat;
        
        @Schema(description = "Longitude", example = "-74.0060")
        private Double lng;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @Schema(description = "Salary information")
    public static class SalaryResponse {
        @Schema(description = "Minimum salary", example = "80000")
        private Double min;
        
        @Schema(description = "Maximum salary", example = "120000")
        private Double max;
        
        @Schema(description = "Currency", example = "USD")
        private String currency;
        
        @Schema(description = "Salary period", example = "YEARLY")
        private Salary.SalaryPeriod period;
        
        @Schema(description = "Whether salary is negotiable", example = "true")
        private Boolean isNegotiable;
        
        @Schema(description = "Formatted salary string", example = "$80,000 - $120,000 per year")
        private String formatted;
    }
}