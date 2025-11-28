package com.pooja.jobportal.service;

import com.pooja.jobportal.dto.JobRequest;
import com.pooja.jobportal.dto.JobResponse;
import com.pooja.jobportal.dto.PublicJobResponse;
import com.pooja.jobportal.exception.ResourceNotFoundException;
import com.pooja.jobportal.exception.UnauthorizedAccessException;
import com.pooja.jobportal.model.*;
import com.pooja.jobportal.repository.ApplicationRepository;
import com.pooja.jobportal.repository.CompanyRepository;
import com.pooja.jobportal.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JobService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final CompanyRepository companyRepository;
    private final AuditLogService auditLogService;

    /**
     * Create a new job for the company
     */
    public JobResponse createJob(JobRequest jobRequest, Company company) {
        log.info("Creating new job for company: {}", company.getName());

        // Use the authenticated company directly

        // Convert location request to location entity
        Location location = convertToLocation(jobRequest.getLocation());

        // Convert salary request to salary entity
        Salary salary = convertToSalary(jobRequest.getSalary());

        // Sanitize HTML content
        String sanitizedDescription = sanitizeHtml(jobRequest.getDescription());
        String sanitizedResponsibilities = sanitizeHtml(jobRequest.getResponsibilities());
        String sanitizedRequirements = sanitizeHtml(jobRequest.getRequirements());
        String sanitizedBenefits = sanitizeHtml(jobRequest.getBenefits());

        Job job = Job.builder()
                .company(company)
                .title(jobRequest.getTitle())
                .status(jobRequest.getStatus() != null ? jobRequest.getStatus() : JobStatus.PUBLISHED)
                .type(jobRequest.getType())
                .workplaceType(jobRequest.getWorkplaceType() != null ? jobRequest.getWorkplaceType() : WorkplaceType.ONSITE)
                .experienceLevel(jobRequest.getExperienceLevel())
                .salary(salary)
                .location(location)
                .skills(jobRequest.getSkills())
                .applyUrl(jobRequest.getApplyUrl())
                .description(sanitizedDescription)
                .responsibilities(sanitizedResponsibilities)
                .requirements(sanitizedRequirements)
                .benefits(sanitizedBenefits)
                .deadline(jobRequest.getDeadline())
                .isActive(jobRequest.getIsActive() != null ? jobRequest.getIsActive() : true)
                .build();

        Job savedJob = jobRepository.save(job);
        log.info("Job created successfully with ID: {}", savedJob.getId());

        return convertToJobResponse(savedJob);
    }

    /**
     * Get all jobs for a company with pagination
     */
    @Transactional(readOnly = true)
    public Page<JobResponse> getJobsForCompany(Company company, Pageable pageable) {
        log.debug("Fetching jobs for company: {}", company.getName());

        Page<Job> jobs = jobRepository.findByCompany(company, pageable);
        List<JobResponse> jobResponses = jobs.getContent().stream()
                .map(this::convertToJobResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
    }

    /**
     * Get active jobs for a company with pagination
     */
    @Transactional(readOnly = true)
    public Page<JobResponse> getActiveJobsForCompany(Company company, Pageable pageable) {
        log.debug("Fetching active jobs for company: {}", company.getName());

        Page<Job> jobs = jobRepository.findByCompanyAndIsActiveTrue(company, pageable);
        List<JobResponse> jobResponses = jobs.getContent().stream()
                .map(this::convertToJobResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
    }

    /**
     * Get a specific job by ID (only if it belongs to the company)
     */
    @Transactional(readOnly = true)
    public JobResponse getJobById(Long jobId, Company company) {
        log.debug("Fetching job with ID: {} for company: {}", jobId, company.getName());

        Job job = jobRepository.findByIdAndCompany(jobId, company)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        return convertToJobResponse(job);
    }

    /**
     * Update an existing job
     */
    public JobResponse updateJob(Long jobId, JobRequest jobRequest, Company company) {
        log.info("Updating job with ID: {} for company: {}", jobId, company.getName());

        Job existingJob = jobRepository.findByIdAndCompany(jobId, company)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        // Use the authenticated company directly
        existingJob.setCompany(company);

        // Update job fields
        existingJob.setTitle(jobRequest.getTitle());
        existingJob.setStatus(jobRequest.getStatus() != null ? jobRequest.getStatus() : existingJob.getStatus());
        existingJob.setType(jobRequest.getType());
        existingJob.setWorkplaceType(jobRequest.getWorkplaceType() != null ? jobRequest.getWorkplaceType() : existingJob.getWorkplaceType());
        existingJob.setExperienceLevel(jobRequest.getExperienceLevel());
        
        // Update salary if provided
        if (jobRequest.getSalary() != null) {
            existingJob.setSalary(convertToSalary(jobRequest.getSalary()));
        }
        
        // Update location if provided
        if (jobRequest.getLocation() != null) {
            existingJob.setLocation(convertToLocation(jobRequest.getLocation()));
        }
        
        existingJob.setSkills(jobRequest.getSkills());
        existingJob.setApplyUrl(jobRequest.getApplyUrl());
        
        // Sanitize and update HTML content
        if (jobRequest.getDescription() != null) {
            existingJob.setDescription(sanitizeHtml(jobRequest.getDescription()));
        }
        if (jobRequest.getResponsibilities() != null) {
            existingJob.setResponsibilities(sanitizeHtml(jobRequest.getResponsibilities()));
        }
        if (jobRequest.getRequirements() != null) {
            existingJob.setRequirements(sanitizeHtml(jobRequest.getRequirements()));
        }
        if (jobRequest.getBenefits() != null) {
            existingJob.setBenefits(sanitizeHtml(jobRequest.getBenefits()));
        }
        
        existingJob.setDeadline(jobRequest.getDeadline());
        existingJob.setIsActive(jobRequest.getIsActive() != null ? jobRequest.getIsActive() : existingJob.getIsActive());

        Job updatedJob = jobRepository.save(existingJob);
        log.info("Job updated successfully with ID: {}", updatedJob.getId());

        return convertToJobResponse(updatedJob);
    }

    /**
     * Delete a job
     */
    public void deleteJob(Long jobId, Company company) {
        log.info("Deleting job with ID: {} for company: {}", jobId, company.getName());

        Job job = jobRepository.findByIdAndCompany(jobId, company)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        jobRepository.delete(job);
        log.info("Job deleted successfully with ID: {}", jobId);
    }

    /**
     * Search jobs by keyword for a company
     */
    @Transactional(readOnly = true)
    public Page<JobResponse> searchJobs(String keyword, Company company, Pageable pageable) {
        log.debug("Searching jobs with keyword: {} for company: {}", keyword, company.getName());

        Page<Job> jobs = jobRepository.searchJobsByCompany(company, keyword, pageable);
        List<JobResponse> jobResponses = jobs.getContent().stream()
                .map(this::convertToJobResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
    }

    /**
     * Get jobs by type for a company
     */
    @Transactional(readOnly = true)
    public Page<JobResponse> getJobsByType(JobType jobType, Company company, Pageable pageable) {
        log.debug("Fetching jobs of type: {} for company: {}", jobType, company.getName());

        Page<Job> jobs = jobRepository.findByCompanyAndType(company, jobType, pageable);
        List<JobResponse> jobResponses = jobs.getContent().stream()
                .map(this::convertToJobResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
    }

    /**
     * Get jobs with approaching deadlines (within 7 days)
     */
    @Transactional(readOnly = true)
    public List<JobResponse> getJobsWithApproachingDeadlines(Company company) {
        log.debug("Fetching jobs with approaching deadlines for company: {}", company.getName());

        LocalDate currentDate = LocalDate.now();
        LocalDate weekFromNow = currentDate.plusDays(7);

        List<Job> jobs = jobRepository.findJobsWithApproachingDeadline(company, currentDate, weekFromNow);
        return jobs.stream()
                .map(this::convertToJobResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Job entity to JobResponse DTO
     */
    private JobResponse convertToJobResponse(Job job) {
        Long applicationCount = applicationRepository.countByJobId(job.getId());

        return JobResponse.builder()
                .id(job.getId())
                .slug(job.getSlug())
                .title(job.getTitle())
                .status(job.getStatus())
                .company(convertToCompanyResponse(job.getCompany()))
                .location(convertToLocationResponse(job.getLocation()))
                .type(job.getType())
                .typeDisplayName(job.getType().getDisplayName())
                .workplaceType(job.getWorkplaceType())
                .experienceLevel(job.getExperienceLevel())
                .salary(convertToSalaryResponse(job.getSalary()))
                .skills(job.getSkills())
                .viewCount(job.getViewCount())
                .applyUrl(job.getApplyUrl())
                .description(job.getDescription())
                .responsibilities(job.getResponsibilities())
                .requirements(job.getRequirements())
                .benefits(job.getBenefits())
                .postedDate(job.getPostedDate())
                .deadline(job.getDeadline())
                .daysUntilDeadline(job.getDaysUntilDeadline())
                .isActive(job.getIsActive())
                .applicationCount(applicationCount)
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    // Public job search methods for job seekers

    /**
     * Get all active jobs for public viewing
     */
    @Transactional(readOnly = true)
    public Page<PublicJobResponse> getAllActiveJobs(Pageable pageable) {
        log.debug("Fetching all active jobs for public viewing");

        Page<Job> jobs = jobRepository.findByIsActiveTrue(pageable);
        List<PublicJobResponse> jobResponses = jobs.getContent().stream()
                .map(this::convertToPublicJobResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
    }

    /**
     * Search active jobs by location
     */
    @Transactional(readOnly = true)
    public Page<PublicJobResponse> searchJobsByLocation(String location, Pageable pageable) {
        log.debug("Searching active jobs by location: {}", location);

        Page<Job> jobs = jobRepository.findActiveJobsByLocation(location, pageable);
        List<PublicJobResponse> jobResponses = jobs.getContent().stream()
                .map(this::convertToPublicJobResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
    }

    /**
     * Search active jobs by keyword (title or description)
     */
    @Transactional(readOnly = true)
    public Page<PublicJobResponse> searchJobsByKeyword(String keyword, Pageable pageable) {
        log.debug("Searching active jobs by keyword: {}", keyword);

        Page<Job> jobs = jobRepository.findActiveJobsByKeyword(keyword, pageable);
        List<PublicJobResponse> jobResponses = jobs.getContent().stream()
                .map(this::convertToPublicJobResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
    }

    /**
     * Search active jobs by location and keyword
     */
    @Transactional(readOnly = true)
    public Page<PublicJobResponse> searchJobsByLocationAndKeyword(String location, String keyword, Pageable pageable) {
        log.debug("Searching active jobs by location: {} and keyword: {}", location, keyword);

        Page<Job> jobs = jobRepository.findActiveJobsByLocationAndKeyword(location, keyword, pageable);
        List<PublicJobResponse> jobResponses = jobs.getContent().stream()
                .map(this::convertToPublicJobResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
    }

    /**
     * Search active jobs by job type
     */
    @Transactional(readOnly = true)
    public Page<PublicJobResponse> searchJobsByType(JobType jobType, Pageable pageable) {
        log.debug("Searching active jobs by type: {}", jobType);

        Page<Job> jobs = jobRepository.findByIsActiveTrueAndType(jobType, pageable);
        List<PublicJobResponse> jobResponses = jobs.getContent().stream()
                .map(this::convertToPublicJobResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
    }

    /**
     * Search active jobs by job type and location
     */
    @Transactional(readOnly = true)
    public Page<PublicJobResponse> searchJobsByTypeAndLocation(JobType jobType, String location, Pageable pageable) {
        log.debug("Searching active jobs by type: {} and location: {}", jobType, location);

        Page<Job> jobs = jobRepository.findActiveJobsByTypeAndLocation(jobType, location, pageable);
        List<PublicJobResponse> jobResponses = jobs.getContent().stream()
                .map(this::convertToPublicJobResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
    }

    /**
     * Search active jobs by job type and keyword
     */
    @Transactional(readOnly = true)
    public Page<PublicJobResponse> searchJobsByTypeAndKeyword(JobType jobType, String keyword, Pageable pageable) {
        log.debug("Searching active jobs by type: {} and keyword: {}", jobType, keyword);

        Page<Job> jobs = jobRepository.findActiveJobsByTypeAndKeyword(jobType, keyword, pageable);
        List<PublicJobResponse> jobResponses = jobs.getContent().stream()
                .map(this::convertToPublicJobResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
    }

    /**
     * Search active jobs by job type, location, and keyword
     */
    @Transactional(readOnly = true)
    public Page<PublicJobResponse> searchJobsByTypeAndLocationAndKeyword(JobType jobType, String location, String keyword, Pageable pageable) {
        log.debug("Searching active jobs by type: {}, location: {}, and keyword: {}", jobType, location, keyword);

        Page<Job> jobs = jobRepository.findActiveJobsByTypeAndLocationAndKeyword(jobType, location, keyword, pageable);
        List<PublicJobResponse> jobResponses = jobs.getContent().stream()
                .map(this::convertToPublicJobResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
    }

    /**
     * Get a specific active job by ID for public viewing
     */
    @Transactional(readOnly = true)
    public PublicJobResponse getActiveJobById(Long jobId) {
        log.debug("Fetching active job with ID: {} for public viewing", jobId);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        if (!job.getIsActive()) {
            throw new ResourceNotFoundException("Job not found with ID: " + jobId);
        }

        return convertToPublicJobResponse(job);
    }

    /**
     * Convert Job entity to PublicJobResponse DTO
     */
    private PublicJobResponse convertToPublicJobResponse(Job job) {
        Long applicationCount = applicationRepository.countByJobId(job.getId());

        return PublicJobResponse.builder()
                .id(job.getId())
                .slug(job.getSlug())
                .title(job.getTitle())
                .company(convertToCompanyResponse(job.getCompany()))
                .location(convertToLocationResponse(job.getLocation()))
                .type(job.getType())
                .typeDisplayName(job.getType().getDisplayName())
                .workplaceType(job.getWorkplaceType())
                .experienceLevel(job.getExperienceLevel())
                .salary(convertToSalaryResponse(job.getSalary()))
                .skills(job.getSkills())
                .applyUrl(job.getApplyUrl())
                .description(job.getDescription())
                .responsibilities(job.getResponsibilities())
                .requirements(job.getRequirements())
                .benefits(job.getBenefits())
                .postedDate(job.getPostedDate())
                .deadline(job.getDeadline())
                .daysUntilDeadline(job.getDaysUntilDeadline())
                .isActive(job.getIsActive())
                .applicationCount(applicationCount)
                .build();
    }

    // Helper methods for conversion and sanitization

    private Company handleCompany(JobRequest jobRequest, Company company) {
        // For company-based job creation, we use the authenticated company directly
        // No need for validation since the company is already authenticated
        return company;
    }

    private Location convertToLocation(JobRequest.LocationRequest locationRequest) {
        if (locationRequest == null) return null;
        
        Location.Coordinates coordinates = null;
        if (locationRequest.getCoordinates() != null) {
            coordinates = Location.Coordinates.builder()
                    .lat(locationRequest.getCoordinates().getLat())
                    .lng(locationRequest.getCoordinates().getLng())
                    .build();
        }
        
        return Location.builder()
                .city(locationRequest.getCity())
                .state(locationRequest.getState())
                .country(locationRequest.getCountry())
                .zipCode(locationRequest.getZipCode())
                .coordinates(coordinates)
                .build();
    }

    private Salary convertToSalary(JobRequest.SalaryRequest salaryRequest) {
        if (salaryRequest == null) return null;
        
        return Salary.builder()
                .min(salaryRequest.getMin())
                .max(salaryRequest.getMax())
                .currency(salaryRequest.getCurrency())
                .period(salaryRequest.getPeriod())
                .isNegotiable(salaryRequest.getIsNegotiable())
                .build();
    }

    private JobResponse.CompanyResponse convertToCompanyResponse(Company company) {
        if (company == null) return null;
        
        return JobResponse.CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .logoUrl(company.getLogoUrl())
                .website(company.getWebsite())
                .description(company.getDescription())
                .industry(company.getIndustry())
                .size(company.getCompanySize())
                .build();
    }

    private JobResponse.LocationResponse convertToLocationResponse(Location location) {
        if (location == null) return null;
        
        JobResponse.CoordinatesResponse coordinates = null;
        if (location.getCoordinates() != null) {
            coordinates = JobResponse.CoordinatesResponse.builder()
                    .lat(location.getCoordinates().getLat())
                    .lng(location.getCoordinates().getLng())
                    .build();
        }
        
        return JobResponse.LocationResponse.builder()
                .city(location.getCity())
                .state(location.getState())
                .country(location.getCountry())
                .zipCode(location.getZipCode())
                .coordinates(coordinates)
                .build();
    }

    private JobResponse.SalaryResponse convertToSalaryResponse(Salary salary) {
        if (salary == null) return null;
        
        String formatted = null;
        if (salary.getMin() != null && salary.getMax() != null) {
            formatted = String.format("$%,.0f - $%,.0f per %s", salary.getMin(), salary.getMax(), 
                    salary.getPeriod().toString().toLowerCase());
        } else if (salary.getMin() != null) {
            formatted = String.format("$%,.0f+ per %s", salary.getMin(), 
                    salary.getPeriod().toString().toLowerCase());
        }
        
        return JobResponse.SalaryResponse.builder()
                .min(salary.getMin())
                .max(salary.getMax())
                .currency(salary.getCurrency())
                .period(salary.getPeriod())
                .isNegotiable(salary.getIsNegotiable())
                .formatted(formatted)
                .build();
    }

    private String sanitizeHtml(String html) {
        if (html == null || html.trim().isEmpty()) {
            return null;
        }
        // Use Jsoup to clean HTML, allowing basic formatting tags
        return Jsoup.clean(html, Safelist.basic());
    }

    // User-based methods for backward compatibility
    
    /**
     * Create a new job for the user (recruiter)
     */
    public JobResponse createJob(JobRequest jobRequest, User user) {
        // For now, we need to handle the User to Company conversion
        // This is a placeholder implementation
        throw new UnsupportedOperationException("User-based job creation is not supported. Please use Company-based methods.");
    }

    /**
     * Get all jobs for a user (admin) with pagination
     */
    @Transactional(readOnly = true)
    public Page<JobResponse> getJobsForAdmin(User user, Pageable pageable) {
        // For now, return all jobs for admin
        Page<Job> jobs = jobRepository.findAll(pageable);
        List<JobResponse> jobResponses = jobs.getContent().stream()
                .map(this::convertToJobResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
    }

    /**
     * Get active jobs for a user (admin) with pagination
     */
    @Transactional(readOnly = true)
    public Page<JobResponse> getActiveJobsForAdmin(User user, Pageable pageable) {
        // For now, return all active jobs for admin
        Page<Job> jobs = jobRepository.findByIsActiveTrue(pageable);
        List<JobResponse> jobResponses = jobs.getContent().stream()
                .map(this::convertToJobResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
    }

    /**
     * Get a specific job by ID (only if it belongs to the user)
     */
    @Transactional(readOnly = true)
    public JobResponse getJobById(Long jobId, User user) {
        // For now, we need to handle the User to Company conversion
        throw new UnsupportedOperationException("User-based job retrieval is not supported. Please use Company-based methods.");
    }

    /**
     * Update an existing job
     */
    public JobResponse updateJob(Long jobId, JobRequest jobRequest, User user) {
        // For now, we need to handle the User to Company conversion
        throw new UnsupportedOperationException("User-based job update is not supported. Please use Company-based methods.");
    }

    /**
     * Delete a job
     */
    public void deleteJob(Long jobId, User user) {
        // For now, we need to handle the User to Company conversion
        throw new UnsupportedOperationException("User-based job deletion is not supported. Please use Company-based methods.");
    }

    /**
     * Search jobs by keyword for a user
     */
    @Transactional(readOnly = true)
    public Page<JobResponse> searchJobs(String keyword, User user, Pageable pageable) {
        // For now, return all active jobs matching the keyword
        Page<Job> jobs = jobRepository.findActiveJobsByKeyword(keyword, pageable);
        List<JobResponse> jobResponses = jobs.getContent().stream()
                .map(this::convertToJobResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
    }

    /**
     * Get jobs by type for a user
     */
    @Transactional(readOnly = true)
    public Page<JobResponse> getJobsByType(JobType jobType, User user, Pageable pageable) {
        // For now, return all active jobs of the specified type
        Page<Job> jobs = jobRepository.findByIsActiveTrueAndType(jobType, pageable);
        List<JobResponse> jobResponses = jobs.getContent().stream()
                .map(this::convertToJobResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
    }

    /**
     * Get jobs with approaching deadlines (within 7 days) for a user
     */
    @Transactional(readOnly = true)
    public List<JobResponse> getJobsWithApproachingDeadlines(User user) {
        // For now, return all jobs with approaching deadlines
        LocalDate currentDate = LocalDate.now();
        LocalDate weekFromNow = currentDate.plusDays(7);

        // Use the admin-based method to get jobs with approaching deadlines
        List<Job> jobs = jobRepository.findJobsWithApproachingDeadline(user, currentDate, weekFromNow);
        return jobs.stream()
                .map(this::convertToJobResponse)
                .collect(Collectors.toList());
    }
}