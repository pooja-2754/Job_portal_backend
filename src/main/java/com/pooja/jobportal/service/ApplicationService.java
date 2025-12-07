package com.pooja.jobportal.service;

import com.pooja.jobportal.dto.ApplicationRequest;
import com.pooja.jobportal.dto.ApplicationResponse;
import com.pooja.jobportal.dto.ApplicationStatusUpdateRequest;
import com.pooja.jobportal.dto.StreamlinedApplicationRequest;
import com.pooja.jobportal.exception.DuplicateApplicationException;
import com.pooja.jobportal.exception.IncompleteProfileException;
import com.pooja.jobportal.exception.ResourceNotFoundException;
import com.pooja.jobportal.exception.UnauthorizedAccessException;
import com.pooja.jobportal.model.Application;
import com.pooja.jobportal.model.ApplicationStatus;
import com.pooja.jobportal.model.Company;
import com.pooja.jobportal.model.Job;
import com.pooja.jobportal.model.Resume;
import com.pooja.jobportal.model.User;
import com.pooja.jobportal.repository.ApplicationRepository;
import com.pooja.jobportal.repository.JobRepository;
import com.pooja.jobportal.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;

    /**
     * Create a new application for a job
     */
    public ApplicationResponse createApplication(ApplicationRequest applicationRequest) {
        log.info("Creating new application for job ID: {} from applicant: {}", 
                applicationRequest.getJobId(), applicationRequest.getApplicantEmail());

        // Verify job exists and is active
        Job job = jobRepository.findById(applicationRequest.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + applicationRequest.getJobId()));

        if (!job.getIsActive()) {
            throw new UnauthorizedAccessException("Job is not active for applications");
        }

        // Check if application already exists for this email
        boolean existingApplication = applicationRepository.existsByJobIdAndApplicantEmail(
                applicationRequest.getJobId(), applicationRequest.getApplicantEmail());
        if (existingApplication) {
            throw new UnauthorizedAccessException("Application already exists for this job and email");
        }

        Application application = Application.builder()
                .job(job)
                .applicantName(applicationRequest.getApplicantName())
                .applicantEmail(applicationRequest.getApplicantEmail())
                .applicantPhone(applicationRequest.getApplicantPhone())
                .resumeUrl(applicationRequest.getResumeUrl())
                .coverLetter(applicationRequest.getCoverLetter())
                .experience(applicationRequest.getExperience())
                .education(applicationRequest.getEducation())
                .status(ApplicationStatus.PENDING)
                .build();

        Application savedApplication = applicationRepository.save(application);
        log.info("Application created successfully with ID: {}", savedApplication.getId());

        return convertToApplicationResponse(savedApplication);
    }

    /**
     * Create a new application for an authenticated candidate
     */
    public ApplicationResponse applyAsCandidate(Long jobId, String coverLetter, User currentUser) {
        log.info("Creating new application for job ID: {} from authenticated user: {}",
                jobId, currentUser.getEmail());

        // Verify job exists and is active
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        if (!job.getIsActive()) {
            throw new UnauthorizedAccessException("Job is not active for applications");
        }

        // Check if user has already applied
        if (applicationRepository.existsByJobAndCandidate(job, currentUser)) {
            throw new DuplicateApplicationException("You have already applied to this job");
        }

        // Validate Candidate Profile (Ensure they have a resume)
        if (currentUser.getPrimaryResume() == null) {
            throw new IncompleteProfileException("Please upload a resume to your profile before applying.");
        }

        // Create Application using Profile Data
        Application application = Application.builder()
                .job(job)
                .candidate(currentUser) // Link to user
                .applicantName(currentUser.getName())
                .applicantEmail(currentUser.getEmail())
                .applicantPhone(currentUser.getPhone())
                .resumeUrl(currentUser.getPrimaryResume().getFileUrl())
                .coverLetter(coverLetter)
                .status(ApplicationStatus.PENDING)
                .build();

        Application savedApplication = applicationRepository.save(application);
        log.info("Application created successfully with ID: {} for user: {}",
                savedApplication.getId(), currentUser.getEmail());

        return convertToApplicationResponse(savedApplication);
    }

    /**
     * Create a new application for an authenticated candidate with custom resume
     */
    public ApplicationResponse applyAsCandidateWithCustomResume(StreamlinedApplicationRequest request, User currentUser) {
        log.info("Creating new application for job ID: {} from authenticated user: {} with custom resume",
                request.getJobId(), currentUser.getEmail());

        // Verify job exists and is active
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + request.getJobId()));

        if (!job.getIsActive()) {
            throw new UnauthorizedAccessException("Job is not active for applications");
        }

        // Check if user has already applied
        if (applicationRepository.existsByJobAndCandidate(job, currentUser)) {
            throw new DuplicateApplicationException("You have already applied to this job");
        }

        // Validate Candidate Profile (Ensure they have a resume either in profile or custom)
        String resumeUrl;
        if (request.getCustomResumeUrl() != null && !request.getCustomResumeUrl().isEmpty()) {
            resumeUrl = request.getCustomResumeUrl();
        } else if (currentUser.getPrimaryResume() != null) {
            resumeUrl = currentUser.getPrimaryResume().getFileUrl();
        } else {
            throw new IncompleteProfileException("Please upload a resume to your profile or provide a custom resume.");
        }

        // Create Application using Profile Data
        Application application = Application.builder()
                .job(job)
                .candidate(currentUser) // Link to user
                .applicantName(currentUser.getName())
                .applicantEmail(currentUser.getEmail())
                .applicantPhone(currentUser.getPhone())
                .resumeUrl(resumeUrl)
                .coverLetter(request.getCoverLetter())
                .status(ApplicationStatus.PENDING)
                .build();

        Application savedApplication = applicationRepository.save(application);
        log.info("Application created successfully with ID: {} for user: {}",
                savedApplication.getId(), currentUser.getEmail());

        return convertToApplicationResponse(savedApplication);
    }

    /**
     * Get all applications for jobs posted by a recruiter
     */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplicationsForRecruiter(User recruiter, Pageable pageable) {
        log.debug("Fetching applications for recruiter: {}", recruiter.getEmail());

        Page<Application> applications = applicationRepository.findByRecruiter(recruiter, pageable);
        List<ApplicationResponse> applicationResponses = applications.getContent().stream()
                .map(this::convertToApplicationResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(applicationResponses, pageable, applications.getTotalElements());
    }

    /**
     * Get applications for a specific job (only if job belongs to the recruiter)
     */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplicationsByJob(Long jobId, User recruiter, Pageable pageable) {
        log.debug("Fetching applications for job ID: {} for recruiter: {}", jobId, recruiter.getEmail());

        Page<Application> applications = applicationRepository.findByJobIdAndRecruiter(jobId, recruiter, pageable);
        List<ApplicationResponse> applicationResponses = applications.getContent().stream()
                .map(this::convertToApplicationResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(applicationResponses, pageable, applications.getTotalElements());
    }

    /**
     * Get applications by status for jobs posted by a recruiter
     */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplicationsByStatus(ApplicationStatus status, User recruiter, Pageable pageable) {
        log.debug("Fetching applications with status: {} for recruiter: {}", status, recruiter.getEmail());

        Page<Application> applications = applicationRepository.findByRecruiterAndStatus(recruiter, status, pageable);
        List<ApplicationResponse> applicationResponses = applications.getContent().stream()
                .map(this::convertToApplicationResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(applicationResponses, pageable, applications.getTotalElements());
    }

    /**
     * Get a specific application by ID (only if job belongs to the recruiter)
     */
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long applicationId, User recruiter) {
        log.debug("Fetching application with ID: {} for recruiter: {}", applicationId, recruiter.getEmail());

        Application application = applicationRepository.findByIdAndRecruiter(applicationId, recruiter)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        return convertToApplicationResponse(application);
    }

    /**
     * Update application status
     */
    public ApplicationResponse updateApplicationStatus(Long applicationId, ApplicationStatusUpdateRequest statusUpdateRequest, User recruiter) {
        log.info("Updating status for application ID: {} to: {} for recruiter: {}", 
                applicationId, statusUpdateRequest.getStatus(), recruiter.getEmail());

        Application application = applicationRepository.findByIdAndRecruiter(applicationId, recruiter)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        application.setStatus(statusUpdateRequest.getStatus());
        Application updatedApplication = applicationRepository.save(application);

        log.info("Application status updated successfully for ID: {}", updatedApplication.getId());

        return convertToApplicationResponse(updatedApplication);
    }

    /**
     * Search applications by keyword for a recruiter
     */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> searchApplications(String keyword, User recruiter, Pageable pageable) {
        log.debug("Searching applications with keyword: {} for recruiter: {}", keyword, recruiter.getEmail());

        Page<Application> applications = applicationRepository.searchApplicationsByRecruiter(recruiter, keyword, pageable);
        List<ApplicationResponse> applicationResponses = applications.getContent().stream()
                .map(this::convertToApplicationResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(applicationResponses, pageable, applications.getTotalElements());
    }

    /**
     * Get recent applications for a recruiter
     */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getRecentApplications(User recruiter, Pageable pageable) {
        log.debug("Fetching recent applications for recruiter: {}", recruiter.getEmail());

        Page<Application> applications = applicationRepository.findRecentApplicationsByRecruiter(recruiter, pageable);
        List<ApplicationResponse> applicationResponses = applications.getContent().stream()
                .map(this::convertToApplicationResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(applicationResponses, pageable, applications.getTotalElements());
    }

    // Company-based methods (replacing recruiter-based methods)
    
    /**
     * Get all applications for jobs posted by a company
     */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplicationsForCompany(Company company, Pageable pageable) {
        log.debug("Fetching applications for company: {}", company.getName());

        Page<Application> applications = applicationRepository.findByCompany(company, pageable);
        List<ApplicationResponse> applicationResponses = applications.getContent().stream()
                .map(this::convertToApplicationResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(applicationResponses, pageable, applications.getTotalElements());
    }

    /**
     * Get applications for a specific job (only if job belongs to company)
     */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplicationsByJob(Long jobId, Company company, Pageable pageable) {
        log.debug("Fetching applications for job ID: {} for company: {}", jobId, company.getName());

        Page<Application> applications = applicationRepository.findByJobIdAndCompany(jobId, company, pageable);
        List<ApplicationResponse> applicationResponses = applications.getContent().stream()
                .map(this::convertToApplicationResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(applicationResponses, pageable, applications.getTotalElements());
    }

    /**
     * Get applications by status for jobs posted by a company
     */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplicationsByStatus(ApplicationStatus status, Company company, Pageable pageable) {
        log.debug("Fetching applications with status: {} for company: {}", status, company.getName());

        Page<Application> applications = applicationRepository.findByCompanyAndStatus(company, status, pageable);
        List<ApplicationResponse> applicationResponses = applications.getContent().stream()
                .map(this::convertToApplicationResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(applicationResponses, pageable, applications.getTotalElements());
    }

    /**
     * Get recent applications for jobs posted by a company
     */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getRecentApplicationsByCompany(Company company, Pageable pageable) {
        log.debug("Fetching recent applications for company: {}", company.getName());

        Page<Application> applications = applicationRepository.findRecentApplicationsByCompany(company, pageable);
        List<ApplicationResponse> applicationResponses = applications.getContent().stream()
                .map(this::convertToApplicationResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(applicationResponses, pageable, applications.getTotalElements());
    }

    /**
     * Get old pending applications that need attention
     */
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getOldPendingApplications(Company company, int daysThreshold) {
        log.debug("Fetching old pending applications for company: {}", company.getName());

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysThreshold);
        List<Application> applications = applicationRepository.findOldPendingApplicationsByCompany(company, cutoffDate);
        
        return applications.stream()
                .map(this::convertToApplicationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific application by ID (only if job belongs to company)
     */
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long applicationId, Company company) {
        log.debug("Fetching application with ID: {} for company: {}", applicationId, company.getName());

        Application application = applicationRepository.findByIdAndCompany(applicationId, company)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        return convertToApplicationResponse(application);
    }

    /**
     * Update application status for company-owned job
     */
    public ApplicationResponse updateApplicationStatus(Long applicationId, ApplicationStatusUpdateRequest statusUpdateRequest, Company company) {
        log.info("Updating status for application ID: {} to: {} for company: {}",
                applicationId, statusUpdateRequest.getStatus(), company.getName());

        Application application = applicationRepository.findByIdAndCompanyForStatusUpdate(applicationId, company)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        application.setStatus(statusUpdateRequest.getStatus());
        Application updatedApplication = applicationRepository.save(application);

        log.info("Application status updated successfully for ID: {}", updatedApplication.getId());

        return convertToApplicationResponse(updatedApplication);
    }

    /**
     * Search applications by keyword for a company
     */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> searchApplications(String keyword, Company company, Pageable pageable) {
        log.debug("Searching applications with keyword: {} for company: {}", keyword, company.getName());

        Page<Application> applications = applicationRepository.searchApplicationsByCompany(company, keyword, pageable);
        List<ApplicationResponse> applicationResponses = applications.getContent().stream()
                .map(this::convertToApplicationResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(applicationResponses, pageable, applications.getTotalElements());
    }

    /**
     * Get all applications for a specific candidate
     */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplicationsForCandidate(User candidate, Pageable pageable) {
        log.debug("Fetching applications for candidate: {}", candidate.getEmail());

        Page<Application> applications = applicationRepository.findByCandidate(candidate, pageable);
        List<ApplicationResponse> applicationResponses = applications.getContent().stream()
                .map(this::convertToApplicationResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(applicationResponses, pageable, applications.getTotalElements());
    }
    
    /**
     * Get recent applications for a candidate
     */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getRecentApplicationsForCandidate(User candidate, Pageable pageable) {
        log.debug("Fetching recent applications for candidate: {}", candidate.getEmail());

        Page<Application> applications = applicationRepository.findRecentApplicationsByCandidate(candidate, pageable);
        List<ApplicationResponse> applicationResponses = applications.getContent().stream()
                .map(this::convertToApplicationResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(applicationResponses, pageable, applications.getTotalElements());
    }

    /**
     * Convert Application entity to ApplicationResponse DTO
     */
    private ApplicationResponse convertToApplicationResponse(Application application) {
        String companyName = null;
        if (application.getJob().getCompany() != null) {
            companyName = application.getJob().getCompany().getName();
        }
        
        String resumeUrl = application.getResumeUrl();
        String resumePreviewUrl = null;
        
        // Try to get preview URL from candidate's primary resume if available
        if (application.getCandidate() != null && application.getCandidate().getPrimaryResume() != null) {
            resumePreviewUrl = application.getCandidate().getPrimaryResume().getPreviewUrl();
        }
        
        return ApplicationResponse.builder()
                .id(application.getId())
                .jobId(application.getJob().getId())
                .jobTitle(application.getJob().getTitle())
                .jobCompany(companyName)
                .applicantName(application.getApplicantName())
                .applicantEmail(application.getApplicantEmail())
                .applicantPhone(application.getApplicantPhone())
                .resumeUrl(resumeUrl)
                .resumePreviewUrl(resumePreviewUrl)
                .coverLetter(application.getCoverLetter())
                .experience(application.getExperience())
                .education(application.getEducation())
                .status(application.getStatus())
                .statusDisplayName(application.getStatus().getDisplayName())
                .appliedDate(application.getAppliedDate())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}