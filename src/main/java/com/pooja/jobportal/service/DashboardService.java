package com.pooja.jobportal.service;

import com.pooja.jobportal.dto.ApplicationResponse;
import com.pooja.jobportal.dto.DashboardStatsResponse;
import com.pooja.jobportal.model.ApplicationStatus;
import com.pooja.jobportal.model.Company;
import com.pooja.jobportal.model.Job;
import com.pooja.jobportal.model.Role;
import com.pooja.jobportal.model.User;
import com.pooja.jobportal.repository.ApplicationRepository;
import com.pooja.jobportal.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationService applicationService;

    /**
     * Get comprehensive dashboard statistics for a company
     * NOTE: This replaces the old recruiter-based dashboard
     */
    public DashboardStatsResponse getCompanyDashboardStats(Company company) {
        log.debug("Generating dashboard statistics for company: {}", company.getName());

        // Job statistics for company
        long totalJobs = jobRepository.countByCompany(company);
        long activeJobs = jobRepository.countByCompanyAndIsActiveTrue(company);
        long inactiveJobs = totalJobs - activeJobs;
        long expiredJobs = jobRepository.countExpiredJobsByCompany(company, LocalDate.now());

        // Application statistics for company's jobs
        long totalApplications = applicationRepository.countByCompany(company);
        long pendingApplications = applicationRepository.countPendingApplicationsByCompany(company);
        long underReviewApplications = applicationRepository.countByCompanyAndStatus(company, ApplicationStatus.UNDER_REVIEW);
        long shortlistedApplications = applicationRepository.countByCompanyAndStatus(company, ApplicationStatus.SHORTLISTED);
        long rejectedApplications = applicationRepository.countByCompanyAndStatus(company, ApplicationStatus.REJECTED);
        long acceptedApplications = applicationRepository.countByCompanyAndStatus(company, ApplicationStatus.ACCEPTED);

        // Application status distribution
        Map<String, Long> applicationStatusDistribution = getApplicationStatusDistributionForCompany(company);

        // Top jobs by applications
        List<DashboardStatsResponse.JobApplicationCount> topJobsByApplications = getTopJobsByApplicationsForCompany(company);

        // Recent applications
        List<ApplicationResponse> recentApplications = getRecentApplicationsForCompany(company);

        // Jobs with approaching deadlines
        List<DashboardStatsResponse.JobDeadlineAlert> jobsWithApproachingDeadlines = getJobsWithApproachingDeadlinesForCompany(company);

        return DashboardStatsResponse.builder()
                .totalJobs(totalJobs)
                .activeJobs(activeJobs)
                .inactiveJobs(inactiveJobs)
                .expiredJobs(expiredJobs)
                .totalApplications(totalApplications)
                .pendingApplications(pendingApplications)
                .underReviewApplications(underReviewApplications)
                .shortlistedApplications(shortlistedApplications)
                .rejectedApplications(rejectedApplications)
                .acceptedApplications(acceptedApplications)
                .applicationStatusDistribution(applicationStatusDistribution)
                .topJobsByApplications(topJobsByApplications)
                .recentApplications(recentApplications)
                .jobsWithApproachingDeadlines(jobsWithApproachingDeadlines)
                .build();
    }

    /**
     * Get application status distribution as a map for a company
     */
    private Map<String, Long> getApplicationStatusDistributionForCompany(Company company) {
        List<Object[]> results = applicationRepository.getApplicationStatusDistributionByCompany(company);
        Map<String, Long> distribution = new HashMap<>();

        // Initialize all statuses with 0
        for (ApplicationStatus status : ApplicationStatus.values()) {
            distribution.put(status.getDisplayName(), 0L);
        }

        // Update with actual counts
        for (Object[] result : results) {
            ApplicationStatus status = (ApplicationStatus) result[0];
            Long count = (Long) result[1];
            distribution.put(status.getDisplayName(), count);
        }

        return distribution;
    }

    /**
     * Get top 5 jobs with most applications for a company
     */
    private List<DashboardStatsResponse.JobApplicationCount> getTopJobsByApplicationsForCompany(Company company) {
        List<Object[]> results = applicationRepository.getApplicationCountPerJobByCompany(company);
        
        return results.stream()
                .limit(5)
                .map(result -> new DashboardStatsResponse.JobApplicationCount(
                        (Long) result[0],  // job ID
                        (String) result[1], // job title
                        (Long) result[2]   // application count
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get recent applications (last 5) for a company
     */
    private List<ApplicationResponse> getRecentApplicationsForCompany(Company company) {
        // Using a small page size to get just the most recent 5
        org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(0, 5, 
                        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "appliedDate"));
        
        return applicationService.getRecentApplicationsByCompany(company, pageable).getContent();
    }

    /**
     * Get jobs with approaching deadlines (within 7 days) for a company
     */
    private List<DashboardStatsResponse.JobDeadlineAlert> getJobsWithApproachingDeadlinesForCompany(Company company) {
        List<Job> jobs = jobRepository.findJobsWithApproachingDeadline(
                company, LocalDate.now(), LocalDate.now().plusDays(7));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return jobs.stream()
                .map(job -> {
                    long daysUntilDeadline = java.time.temporal.ChronoUnit.DAYS.between(
                            LocalDate.now(), job.getDeadline());
                     
                    return new DashboardStatsResponse.JobDeadlineAlert(
                            job.getId(),
                            job.getTitle(),
                            daysUntilDeadline,
                            job.getDeadline().format(formatter)
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Get application status distribution as a map for a candidate
     */
    private Map<String, Long> getApplicationStatusDistributionForCandidate(User candidate) {
        List<Object[]> results = applicationRepository.getApplicationStatusDistributionByCandidate(candidate);
        Map<String, Long> distribution = new HashMap<>();

        // Initialize all statuses with 0
        for (ApplicationStatus status : ApplicationStatus.values()) {
            distribution.put(status.getDisplayName(), 0L);
        }

        // Update with actual counts
        for (Object[] result : results) {
            ApplicationStatus status = (ApplicationStatus) result[0];
            Long count = (Long) result[1];
            distribution.put(status.getDisplayName(), count);
        }

        return distribution;
    }

    /**
     * Get recent applications (last 5) for a candidate
     */
    private List<ApplicationResponse> getRecentApplicationsForCandidate(User candidate) {
        // Using a small page size to get just the most recent 5
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, 5,
                        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "appliedDate"));
        
        return applicationService.getRecentApplicationsForCandidate(candidate, pageable).getContent();
    }

    /**
     * Get comprehensive dashboard statistics for an admin
     */
    public DashboardStatsResponse getAdminDashboardStats(User admin) {
        // Validate that user is an admin
        if (admin.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Access denied. Only administrators can access admin dashboard statistics.");
        }
        
        log.debug("Generating admin dashboard statistics for: {}", admin.getEmail());

        // System-wide statistics - simplified for admin dashboard
        long totalJobs = jobRepository.count();
        long activeJobs = jobRepository.countByIsActiveTrue();
        long totalApplications = applicationRepository.count();

        return DashboardStatsResponse.builder()
                .totalJobs(totalJobs)
                .activeJobs(activeJobs)
                .totalApplications(totalApplications)
                .build();
    }

    /**
     * Get comprehensive dashboard statistics for a job seeker
     */
    public DashboardStatsResponse getJobSeekerDashboardStats(User jobSeeker) {
        // Validate that user is a job seeker
        if (jobSeeker.getRole() != Role.JOB_SEEKER) {
            throw new IllegalArgumentException("Access denied. Only job seekers can access job seeker dashboard statistics.");
        }
        
        log.debug("Generating job seeker dashboard statistics for: {}", jobSeeker.getEmail());

        // Application statistics for the job seeker
        long totalApplications = applicationRepository.countByCandidate(jobSeeker);
        long pendingApplications = applicationRepository.countByCandidateAndStatus(jobSeeker, ApplicationStatus.PENDING);
        long underReviewApplications = applicationRepository.countByCandidateAndStatus(jobSeeker, ApplicationStatus.UNDER_REVIEW);
        long shortlistedApplications = applicationRepository.countByCandidateAndStatus(jobSeeker, ApplicationStatus.SHORTLISTED);
        long rejectedApplications = applicationRepository.countByCandidateAndStatus(jobSeeker, ApplicationStatus.REJECTED);
        long acceptedApplications = applicationRepository.countByCandidateAndStatus(jobSeeker, ApplicationStatus.ACCEPTED);

        // Application status distribution
        Map<String, Long> applicationStatusDistribution = getApplicationStatusDistributionForCandidate(jobSeeker);

        // Recent applications
        List<ApplicationResponse> recentApplications = getRecentApplicationsForCandidate(jobSeeker);

        // Job market statistics (system-wide)
        long totalActiveJobs = jobRepository.countByIsActiveTrue();

        return DashboardStatsResponse.builder()
                .totalApplications(totalApplications)
                .pendingApplications(pendingApplications)
                .underReviewApplications(underReviewApplications)
                .shortlistedApplications(shortlistedApplications)
                .rejectedApplications(rejectedApplications)
                .acceptedApplications(acceptedApplications)
                .applicationStatusDistribution(applicationStatusDistribution)
                .recentApplications(recentApplications)
                .activeJobs(totalActiveJobs) // Using activeJobs field to show total active jobs in system
                .totalJobs(totalActiveJobs) // Using totalJobs field to show total active jobs in system
                .build();
    }

    /**
     * Get comprehensive dashboard statistics for a user (recruiter or admin)
     * This method determines the appropriate dashboard based on user role
     */
    public DashboardStatsResponse getDashboardStats(User user) {
        if (user.getRole() == Role.ADMIN) {
            return getAdminDashboardStats(user);
        } else if (user.getRole() == Role.JOB_SEEKER) {
            return getJobSeekerDashboardStats(user);
        } else {
            // For recruiters, we would need to implement a recruiter-specific dashboard
            // For now, return a basic implementation
            return DashboardStatsResponse.builder()
                    .totalJobs(0L) // Placeholder
                    .activeJobs(0L) // Placeholder
                    .totalApplications(0L) // Placeholder
                    .pendingApplications(0L) // Placeholder
                    .underReviewApplications(0L) // Placeholder
                    .shortlistedApplications(0L) // Placeholder
                    .rejectedApplications(0L) // Placeholder
                    .acceptedApplications(0L) // Placeholder
                    .build();
        }
    }

    // Note: Job seeker dashboard methods would need additional repository methods
    // For now, we'll keep the simplified implementation above
}