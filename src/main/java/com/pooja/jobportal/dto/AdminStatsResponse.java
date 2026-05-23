package com.pooja.jobportal.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStatsResponse {
    private long totalUsers;
    private long totalCompanies;
    private long totalJobs;
    private long totalApplications;
    private long pendingCompanies;
    private long verifiedCompanies;
    private long activeJobs;
}
