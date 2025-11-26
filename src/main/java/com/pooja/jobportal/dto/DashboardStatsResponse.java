package com.pooja.jobportal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "Response object for dashboard statistics")
public class DashboardStatsResponse {
    
    @Schema(description = "Total number of jobs posted by the recruiter", example = "25")
    private Long totalJobs;
    
    @Schema(description = "Number of currently active jobs", example = "18")
    private Long activeJobs;
    
    @Schema(description = "Number of inactive jobs", example = "7")
    private Long inactiveJobs;
    
    @Schema(description = "Number of expired jobs", example = "3")
    private Long expiredJobs;
    
    @Schema(description = "Total number of applications received", example = "156")
    private Long totalApplications;
    
    @Schema(description = "Number of pending applications", example = "42")
    private Long pendingApplications;
    
    @Schema(description = "Number of applications under review", example = "28")
    private Long underReviewApplications;
    
    @Schema(description = "Number of shortlisted applications", example = "15")
    private Long shortlistedApplications;
    
    @Schema(description = "Number of rejected applications", example = "65")
    private Long rejectedApplications;
    
    @Schema(description = "Number of accepted applications", example = "6")
    private Long acceptedApplications;
    
    @Schema(description = "Application status distribution")
    private Map<String, Long> applicationStatusDistribution;
    
    @Schema(description = "Top 5 jobs with most applications")
    private List<JobApplicationCount> topJobsByApplications;
    
    @Schema(description = "Recent applications (last 5)")
    private List<ApplicationResponse> recentApplications;
    
    @Schema(description = "Jobs with approaching deadlines (within 7 days)")
    private List<JobDeadlineAlert> jobsWithApproachingDeadlines;
    
    @Data
    @Builder
    @AllArgsConstructor
    @Schema(description = "Job with application count")
    public static class JobApplicationCount {
        @Schema(description = "Job ID", example = "5")
        private Long jobId;
        
        @Schema(description = "Job title", example = "Senior Java Developer")
        private String jobTitle;
        
        @Schema(description = "Number of applications", example = "23")
        private Long applicationCount;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @Schema(description = "Job with deadline alert")
    public static class JobDeadlineAlert {
        @Schema(description = "Job ID", example = "8")
        private Long jobId;
        
        @Schema(description = "Job title", example = "Frontend Developer")
        private String jobTitle;
        
        @Schema(description = "Days until deadline", example = "3")
        private Long daysUntilDeadline;
        
        @Schema(description = "Application deadline", example = "2024-01-25")
        private String deadline;
    }
}