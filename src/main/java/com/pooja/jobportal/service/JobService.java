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

    /**
     * Create a new job for the recruiter
     */
    public JobResponse createJob(JobRequest jobRequest, User recruiter) {
        log.info("Creating new job for recruiter: {}", recruiter.getEmail());

        // Handle company - either use existing or create new
        Company company = handleCompany(jobRequest);

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
                .recruiter(recruiter)
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
     * Get all jobs for a recruiter with pagination
     */
    @Transactional(readOnly = true)
    public Page<JobResponse> getJobsForRecruiter(User recruiter, Pageable pageable) {
        log.debug("Fetching jobs for recruiter: {}", recruiter.getEmail());

        Page<Job> jobs = jobRepository.findByRecruiter(recruiter, pageable);
        List<JobResponse> jobResponses = jobs.getContent().stream()
                .map(this::convertToJobResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
    }

    /**
     * Get active jobs for a recruiter with pagination
     */
    @Transactional(readOnly = true)
    public Page<JobResponse> getActiveJobsForRecruiter(User recruiter, Pageable pageable) {
        log.debug("Fetching active jobs for recruiter: {}", recruiter.getEmail());

        Page<Job> jobs = jobRepository.findByRecruiterAndIsActiveTrue(recruiter, pageable);
        List<JobResponse> jobResponses = jobs.getContent().stream()
                .map(this::convertToJobResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
    }

    /**
     * Get a specific job by ID (only if it belongs to the recruiter)
     */
    @Transactional(readOnly = true)
    public JobResponse getJobById(Long jobId, User recruiter) {
        log.debug("Fetching job with ID: {} for recruiter: {}", jobId, recruiter.getEmail());

        Job job = jobRepository.findByIdAndRecruiter(jobId, recruiter)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        return convertToJobResponse(job);
    }

    /**
     * Update an existing job
     */
    public JobResponse updateJob(Long jobId, JobRequest jobRequest, User recruiter) {
        log.info("Updating job with ID: {} for recruiter: {}", jobId, recruiter.getEmail());

        Job existingJob = jobRepository.findByIdAndRecruiter(jobId, recruiter)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        // Handle company - either use existing or create new
        Company company = handleCompany(jobRequest);
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
    public void deleteJob(Long jobId, User recruiter) {
        log.info("Deleting job with ID: {} for recruiter: {}", jobId, recruiter.getEmail());

        Job job = jobRepository.findByIdAndRecruiter(jobId, recruiter)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        jobRepository.delete(job);
        log.info("Job deleted successfully with ID: {}", jobId);
    }

    /**
     * Search jobs by keyword for a recruiter
     */
    @Transactional(readOnly = true)
    public Page<JobResponse> searchJobs(String keyword, User recruiter, Pageable pageable) {
        log.debug("Searching jobs with keyword: {} for recruiter: {}", keyword, recruiter.getEmail());

        Page<Job> jobs = jobRepository.searchJobsByRecruiter(recruiter, keyword, pageable);
        List<JobResponse> jobResponses = jobs.getContent().stream()
                .map(this::convertToJobResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
    }

    /**
     * Get jobs by type for a recruiter
     */
    @Transactional(readOnly = true)
    public Page<JobResponse> getJobsByType(JobType jobType, User recruiter, Pageable pageable) {
        log.debug("Fetching jobs of type: {} for recruiter: {}", jobType, recruiter.getEmail());

        Page<Job> jobs = jobRepository.findByRecruiterAndType(recruiter, jobType, pageable);
        List<JobResponse> jobResponses = jobs.getContent().stream()
                .map(this::convertToJobResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
    }

    /**
     * Get jobs with approaching deadlines (within 7 days)
     */
    @Transactional(readOnly = true)
    public List<JobResponse> getJobsWithApproachingDeadlines(User recruiter) {
        log.debug("Fetching jobs with approaching deadlines for recruiter: {}", recruiter.getEmail());

        LocalDate currentDate = LocalDate.now();
        LocalDate weekFromNow = currentDate.plusDays(7);

        List<Job> jobs = jobRepository.findJobsWithApproachingDeadline(recruiter, currentDate, weekFromNow);
        return jobs.stream()
                .map(this::convertToJobResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Job entity to JobResponse DTO
     */
    private JobResponse convertToJobResponse(Job job) {
        Long applicationCount = applicationRepository.countByJobIdAndRecruiter(job.getId(), job.getRecruiter());

        return JobResponse.builder()
                .id(job.getId())
                .slug(job.getSlug())
                .recruiterId(job.getRecruiter().getId())
                .recruiterName(job.getRecruiter().getName())
                .recruiterEmail(job.getRecruiter().getEmail())
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

    private Company handleCompany(JobRequest jobRequest) {
        if (jobRequest.getCompanyId() != null) {
            // Use existing company
            return companyRepository.findById(jobRequest.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + jobRequest.getCompanyId()));
        } else if (jobRequest.getCompanyName() != null && !jobRequest.getCompanyName().trim().isEmpty()) {
            // Create or find existing company by name
            Optional<Company> existingCompany = companyRepository.findByName(jobRequest.getCompanyName());
            if (existingCompany.isPresent()) {
                return existingCompany.get();
            } else {
                Company newCompany = Company.builder()
                        .name(jobRequest.getCompanyName())
                        .website(jobRequest.getCompanyWebsite())
                        .logoUrl(jobRequest.getCompanyLogoUrl())
                        .description(jobRequest.getCompanyDescription())
                        .industry(jobRequest.getCompanyIndustry())
                        .companySize(jobRequest.getCompanySize())
                        .verificationStatus(CompanyVerificationStatus.PENDING)
                        .build();
                return companyRepository.save(newCompany);
            }
        }
        
        // If no company information is provided, throw an exception
        throw new IllegalArgumentException("Company information is required. Either provide a companyId or companyName.");
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
}