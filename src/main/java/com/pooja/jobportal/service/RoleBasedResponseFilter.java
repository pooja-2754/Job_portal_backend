package com.pooja.jobportal.service;

import com.pooja.jobportal.dto.CompanyResponse;
import com.pooja.jobportal.dto.JobResponse;
import com.pooja.jobportal.model.Role;
import com.pooja.jobportal.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service to filter API responses based on user roles to prevent data leakage
 */
@Service
@Slf4j
public class RoleBasedResponseFilter {

    /**
     * Filter company response based on user role
     * - Job seekers can see basic company info
     * - Recruiters can see full company info if they own it
     * - Admins can see all company info
     */
    public CompanyResponse filterCompanyResponse(CompanyResponse companyResponse, User currentUser, User companyOwner) {
        if (currentUser.getRole() == Role.ADMIN) {
            // Admins can see everything
            return companyResponse;
        }

        // RECRUITER role removed - companies are now self-contained entities
        // This logic will be handled by company authentication

        if (currentUser.getRole() == Role.JOB_SEEKER) {
            // Job seekers can see limited company info
            return CompanyResponse.builder()
                    .id(companyResponse.getId())
                    .name(companyResponse.getName())
                    .logoUrl(companyResponse.getLogoUrl())
                    .website(companyResponse.getWebsite())
                    .description(companyResponse.getDescription())
                    .industry(companyResponse.getIndustry())
                    .companySize(companyResponse.getCompanySize())
                    // Hide sensitive information
                    .verificationStatus(null)
                    .verifiedAt(null)
                    .adminId(null)
                    .adminName(null)
                    .jobCount(companyResponse.getJobCount())
                    .createdAt(companyResponse.getCreatedAt())
                    .updatedAt(companyResponse.getUpdatedAt())
                    .build();
        }

        return companyResponse;
    }

    /**
     * Filter job response based on user role
     * - Job seekers can see basic job info
     * - Recruiters can see full job info for their own jobs
     * - Admins can see all job info
     */
    public JobResponse filterJobResponse(JobResponse jobResponse, User currentUser, User jobOwner) {
        if (currentUser.getRole() == Role.ADMIN) {
            // Admins can see everything
            return jobResponse;
        }

        // RECRUITER role removed - companies are now self-contained entities
        // This logic will be handled by company authentication

        if (currentUser.getRole() == Role.JOB_SEEKER) {
            // Job seekers can see limited job info
            return JobResponse.builder()
                    .id(jobResponse.getId())
                    .slug(jobResponse.getSlug())
                    .title(jobResponse.getTitle())
                    .company(filterJobCompanyResponse(jobResponse.getCompany(), currentUser))
                    .location(jobResponse.getLocation())
                    .type(jobResponse.getType())
                    .typeDisplayName(jobResponse.getTypeDisplayName())
                    .workplaceType(jobResponse.getWorkplaceType())
                    .experienceLevel(jobResponse.getExperienceLevel())
                    .salary(jobResponse.getSalary())
                    .skills(jobResponse.getSkills())
                    .applyUrl(jobResponse.getApplyUrl())
                    .description(jobResponse.getDescription())
                    .responsibilities(jobResponse.getResponsibilities())
                    .requirements(jobResponse.getRequirements())
                    .benefits(jobResponse.getBenefits())
                    .postedDate(jobResponse.getPostedDate())
                    .deadline(jobResponse.getDeadline())
                    .daysUntilDeadline(jobResponse.getDaysUntilDeadline())
                    .isActive(jobResponse.getIsActive())
                    .applicationCount(jobResponse.getApplicationCount())
                    .createdAt(jobResponse.getCreatedAt())
                    .updatedAt(jobResponse.getUpdatedAt())
                    .viewCount(jobResponse.getViewCount())
                    // Hide sensitive information
                    .recruiterId(null)
                    .recruiterName(null)
                    .recruiterEmail(null)
                    .build();
        }

        return jobResponse;
    }

    /**
     * Filter a list of company responses based on user role
     */
    public List<CompanyResponse> filterCompanyResponses(List<CompanyResponse> companies, User currentUser) {
        return companies.stream()
                .map(company -> filterCompanyResponse(company, currentUser, null))
                .toList();
    }

    /**
     * Filter a list of job responses based on user role
     */
    public List<JobResponse> filterJobResponses(List<JobResponse> jobs, User currentUser) {
        return jobs.stream()
                .map(job -> filterJobResponse(job, currentUser, null))
                .toList();
    }

    /**
     * Filter the nested CompanyResponse in JobResponse based on user role
     */
    private JobResponse.CompanyResponse filterJobCompanyResponse(JobResponse.CompanyResponse companyResponse, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            // Admins can see everything
            return companyResponse;
        }

        if (currentUser.getRole() == Role.JOB_SEEKER) {
            // For both recruiters and job seekers, return basic company info
            return JobResponse.CompanyResponse.builder()
                    .id(companyResponse.getId())
                    .name(companyResponse.getName())
                    .logoUrl(companyResponse.getLogoUrl())
                    .website(companyResponse.getWebsite())
                    .description(companyResponse.getDescription())
                    .industry(companyResponse.getIndustry())
                    .size(companyResponse.getSize())
                    .build();
        }

        return companyResponse;
    }
}