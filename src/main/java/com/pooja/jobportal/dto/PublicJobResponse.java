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
@Schema(description = "Response object for public job information (for job seekers)")
public class PublicJobResponse {
    
    @Schema(description = "Unique job identifier", example = "1")
    private Long id;
    
    @Schema(description = "SEO-friendly URL slug", example = "senior-java-developer-tech-corp-ny")
    private String slug;
    
    @Schema(description = "Title of the job position", example = "Senior Java Developer")
    private String title;
    
    @Schema(description = "Company information")
    private JobResponse.CompanyResponse company;
    
    @Schema(description = "Location information")
    private JobResponse.LocationResponse location;
    
    @Schema(description = "Type of employment", example = "FULL_TIME")
    private JobType type;
    
    @Schema(description = "Display name for job type", example = "Full-Time")
    private String typeDisplayName;
    
    @Schema(description = "Work arrangement type", example = "HYBRID")
    private WorkplaceType workplaceType;
    
    @Schema(description = "Experience level required", example = "SENIOR")
    private ExperienceLevel experienceLevel;
    
    @Schema(description = "Salary information")
    private JobResponse.SalaryResponse salary;
    
    @Schema(description = "Skills required for the job")
    private List<String> skills;
    
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
    
    @Schema(description = "Number of days until deadline", example = "15")
    private Long daysUntilDeadline;
    
    @Schema(description = "Whether the job is currently active", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Number of applications received for this job", example = "15")
    private Long applicationCount;
}