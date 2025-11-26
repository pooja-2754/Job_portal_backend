package com.pooja.jobportal.dto;

import com.pooja.jobportal.model.JobType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Request object for creating or updating a job")
public class JobRequest {
    
    @NotBlank(message = "Job title is required")
    @Schema(description = "Title of the job position", example = "Senior Java Developer", required = true)
    private String title;
    
    @NotBlank(message = "Company name is required")
    @Schema(description = "Name of the company offering the job", example = "Tech Corp", required = true)
    private String company;
    
    @NotBlank(message = "Location is required")
    @Schema(description = "Job location", example = "New York, NY", required = true)
    private String location;
    
    @NotNull(message = "Job type is required")
    @Schema(description = "Type of employment", example = "FULL_TIME", required = true)
    private JobType type;
    
    @Schema(description = "Salary range or compensation", example = "$80,000 - $120,000")
    private String salary;
    
    @NotBlank(message = "Job description is required")
    @Schema(description = "Detailed description of the job role and responsibilities", 
            example = "We are looking for an experienced Java developer...", required = true)
    private String description;
    
    @Schema(description = "Required qualifications and skills", 
            example = "5+ years of Java experience, Spring Boot, Microservices...")
    private String requirements;
    
    @Schema(description = "Application deadline", example = "2024-12-31")
    private LocalDate deadline;
    
    @Schema(description = "Whether the job is currently active", example = "true")
    private Boolean isActive = true;
}