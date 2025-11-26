package com.pooja.jobportal.dto;

import com.pooja.jobportal.model.JobType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "Response object for public job information (for job seekers)")
public class PublicJobResponse {
    
    @Schema(description = "Unique job identifier", example = "1")
    private Long id;
    
    @Schema(description = "Title of the job position", example = "Senior Java Developer")
    private String title;
    
    @Schema(description = "Name of the company offering the job", example = "Tech Corp")
    private String company;
    
    @Schema(description = "Job location", example = "New York, NY")
    private String location;
    
    @Schema(description = "Type of employment", example = "FULL_TIME")
    private JobType type;
    
    @Schema(description = "Display name for job type", example = "Full-Time")
    private String typeDisplayName;
    
    @Schema(description = "Salary range or compensation", example = "$80,000 - $120,000")
    private String salary;
    
    @Schema(description = "Detailed description of the job role and responsibilities")
    private String description;
    
    @Schema(description = "Required qualifications and skills")
    private String requirements;
    
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