package com.pooja.jobportal.service;

import com.pooja.jobportal.dto.JobRequest;
import com.pooja.jobportal.dto.JobResponse;
import com.pooja.jobportal.dto.PublicJobResponse;
import com.pooja.jobportal.exception.ResourceNotFoundException;
import com.pooja.jobportal.exception.UnauthorizedAccessException;
import com.pooja.jobportal.model.Job;
import com.pooja.jobportal.model.JobType;
import com.pooja.jobportal.model.User;
import com.pooja.jobportal.repository.ApplicationRepository;
import com.pooja.jobportal.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JobService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    /**
     * Create a new job for the recruiter
     */
    public JobResponse createJob(JobRequest jobRequest, User recruiter) {
        log.info("Creating new job for recruiter: {}", recruiter.getEmail());

        Job job = Job.builder()
                .recruiter(recruiter)
                .title(jobRequest.getTitle())
                .company(jobRequest.getCompany())
                .location(jobRequest.getLocation())
                .type(jobRequest.getType())
                .salary(jobRequest.getSalary())
                .description(jobRequest.getDescription())
                .requirements(jobRequest.getRequirements())
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

        // Update job fields
        existingJob.setTitle(jobRequest.getTitle());
        existingJob.setCompany(jobRequest.getCompany());
        existingJob.setLocation(jobRequest.getLocation());
        existingJob.setType(jobRequest.getType());
        existingJob.setSalary(jobRequest.getSalary());
        existingJob.setDescription(jobRequest.getDescription());
        existingJob.setRequirements(jobRequest.getRequirements());
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
                .recruiterId(job.getRecruiter().getId())
                .recruiterName(job.getRecruiter().getName())
                .recruiterEmail(job.getRecruiter().getEmail())
                .title(job.getTitle())
                .company(job.getCompany())
                .location(job.getLocation())
                .type(job.getType())
                .typeDisplayName(job.getType().getDisplayName())
                .salary(job.getSalary())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .postedDate(job.getPostedDate())
                .deadline(job.getDeadline())
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
        Long daysUntilDeadline = null;
        
        if (job.getDeadline() != null) {
            LocalDate currentDate = LocalDate.now();
            daysUntilDeadline = ChronoUnit.DAYS.between(currentDate, job.getDeadline());
            if (daysUntilDeadline < 0) {
                daysUntilDeadline = 0L;
            }
        }

        return PublicJobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .company(job.getCompany())
                .location(job.getLocation())
                .type(job.getType())
                .typeDisplayName(job.getType().getDisplayName())
                .salary(job.getSalary())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .postedDate(job.getPostedDate())
                .deadline(job.getDeadline())
                .daysUntilDeadline(daysUntilDeadline)
                .isActive(job.getIsActive())
                .applicationCount(applicationCount)
                .build();
    }
}