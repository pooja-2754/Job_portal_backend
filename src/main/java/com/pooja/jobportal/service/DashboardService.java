package com.pooja.jobportal.service;

import com.pooja.jobportal.dto.ApplicationResponse;
import com.pooja.jobportal.dto.DashboardStatsResponse;
import com.pooja.jobportal.model.ApplicationStatus;
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
     * Get comprehensive dashboard statistics for a recruiter
     */
    public DashboardStatsResponse getDashboardStats(User recruiter) {
        // Validate that the user is a recruiter
        if (recruiter.getRole() != Role.RECRUITER) {
            throw new IllegalArgumentException("Access denied. Only recruiters can access dashboard statistics.");
        }
        
        log.debug("Generating dashboard statistics for recruiter: {}", recruiter.getEmail());

        // Job statistics
        long totalJobs = jobRepository.countByRecruiter(recruiter);
        long activeJobs = jobRepository.countByRecruiterAndIsActiveTrue(recruiter);
        long inactiveJobs = totalJobs - activeJobs;
        long expiredJobs = jobRepository.countExpiredJobsByRecruiter(recruiter, LocalDate.now());

        // Application statistics
        long totalApplications = applicationRepository.countByRecruiter(recruiter);
        long pendingApplications = applicationRepository.countPendingApplicationsByRecruiter(recruiter);
        long underReviewApplications = applicationRepository.countByRecruiterAndStatus(recruiter, ApplicationStatus.UNDER_REVIEW);
        long shortlistedApplications = applicationRepository.countByRecruiterAndStatus(recruiter, ApplicationStatus.SHORTLISTED);
        long rejectedApplications = applicationRepository.countByRecruiterAndStatus(recruiter, ApplicationStatus.REJECTED);
        long acceptedApplications = applicationRepository.countByRecruiterAndStatus(recruiter, ApplicationStatus.ACCEPTED);

        // Application status distribution
        Map<String, Long> applicationStatusDistribution = getApplicationStatusDistribution(recruiter);

        // Top jobs by applications
        List<DashboardStatsResponse.JobApplicationCount> topJobsByApplications = getTopJobsByApplications(recruiter);

        // Recent applications
        List<ApplicationResponse> recentApplications = getRecentApplications(recruiter);

        // Jobs with approaching deadlines
        List<DashboardStatsResponse.JobDeadlineAlert> jobsWithApproachingDeadlines = getJobsWithApproachingDeadlines(recruiter);

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
     * Get application status distribution as a map
     */
    private Map<String, Long> getApplicationStatusDistribution(User recruiter) {
        List<Object[]> results = applicationRepository.getApplicationStatusDistributionByRecruiter(recruiter);
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
     * Get top 5 jobs with most applications
     */
    private List<DashboardStatsResponse.JobApplicationCount> getTopJobsByApplications(User recruiter) {
        List<Object[]> results = applicationRepository.getApplicationCountPerJobByRecruiter(recruiter);
        
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
     * Get recent applications (last 5)
     */
    private List<ApplicationResponse> getRecentApplications(User recruiter) {
        // Using a small page size to get just the most recent 5
        org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(0, 5, 
                        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "appliedDate"));
        
        return applicationService.getRecentApplications(recruiter, pageable).getContent();
    }

    /**
     * Get jobs with approaching deadlines (within 7 days)
     */
    private List<DashboardStatsResponse.JobDeadlineAlert> getJobsWithApproachingDeadlines(User recruiter) {
        List<Job> jobs = jobRepository.findJobsWithApproachingDeadline(
                recruiter, LocalDate.now(), LocalDate.now().plusDays(7));

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
}