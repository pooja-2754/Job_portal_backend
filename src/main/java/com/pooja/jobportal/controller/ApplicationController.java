package com.pooja.jobportal.controller;

import com.pooja.jobportal.dto.ApplicationRequest;
import com.pooja.jobportal.dto.ApplicationResponse;
import com.pooja.jobportal.dto.ApplicationStatusUpdateRequest;
import com.pooja.jobportal.dto.StreamlinedApplicationRequest;
import com.pooja.jobportal.model.ApplicationStatus;
import com.pooja.jobportal.model.Company;
import com.pooja.jobportal.model.User;
import com.pooja.jobportal.security.CompanyPrincipal;
import com.pooja.jobportal.security.UserPrincipal;
import com.pooja.jobportal.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Application Management", description = "APIs for managing job applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    @Operation(summary = "Create a new application", description = "Submits a new application for a job")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Application created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data or application already exists"),
        @ApiResponse(responseCode = "404", description = "Job not found or not active")
    })
    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
            @Valid @RequestBody ApplicationRequest applicationRequest) {
        
        ApplicationResponse applicationResponse = applicationService.createApplication(applicationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationResponse);
    }

    @Operation(summary = "Create a new application for authenticated candidate", description = "Submits a new application for a job using authenticated user's profile data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Application created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data, incomplete profile, or application already exists"),
        @ApiResponse(responseCode = "404", description = "Job not found or not active"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - User must be authenticated"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only job seekers can apply")
    })
    @PostMapping("/candidate")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<ApplicationResponse> applyAsCandidate(
            @Valid @RequestBody StreamlinedApplicationRequest applicationRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userPrincipal.getUser();
        ApplicationResponse applicationResponse = applicationService.applyAsCandidateWithCustomResume(
                applicationRequest, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationResponse);
    }

    @Operation(summary = "Get all applications for recruiter", description = "Retrieves all applications for jobs posted by the authenticated recruiter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Applications retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only recruiters can access their applications")
    })
    @GetMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<ApplicationResponse>> getApplications(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "appliedDate")
            @RequestParam(defaultValue = "appliedDate") String sortBy,
            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userPrincipal.getUser();
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ApplicationResponse> applications = applicationService.getApplicationsForRecruiter(user, pageable);
        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "Get applications for a specific job", description = "Retrieves applications for a specific job (only if job belongs to recruiter)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Applications retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Job not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Job doesn't belong to recruiter")
    })
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<ApplicationResponse>> getApplicationsByJob(
            @Parameter(description = "Job ID", example = "1")
            @PathVariable Long jobId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userPrincipal.getUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedDate"));
        Page<ApplicationResponse> applications = applicationService.getApplicationsByJob(jobId, user, pageable);
        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "Get applications by status", description = "Retrieves applications with a specific status for the authenticated recruiter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Applications retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<ApplicationResponse>> getApplicationsByStatus(
            @Parameter(description = "Application status", example = "PENDING")
            @PathVariable ApplicationStatus status,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userPrincipal.getUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedDate"));
        Page<ApplicationResponse> applications = applicationService.getApplicationsByStatus(status, user, pageable);
        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "Get application by ID", description = "Retrieves a specific application by ID (only if job belongs to recruiter)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Application not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Application doesn't belong to recruiter")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @Parameter(description = "Application ID", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userPrincipal.getUser();
        ApplicationResponse application = applicationService.getApplicationById(id, user);
        return ResponseEntity.ok(application);
    }

    @Operation(summary = "Update application status", description = "Updates the status of an application (only if job belongs to recruiter)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status"),
        @ApiResponse(responseCode = "404", description = "Application not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Application doesn't belong to recruiter")
    })
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @Parameter(description = "Application ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ApplicationStatusUpdateRequest statusUpdateRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userPrincipal.getUser();
        ApplicationResponse updatedApplication = applicationService.updateApplicationStatus(
                id, statusUpdateRequest, user);
        return ResponseEntity.ok(updatedApplication);
    }

    @Operation(summary = "Search applications", description = "Search applications by keyword for the authenticated recruiter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/search")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<ApplicationResponse>> searchApplications(
            @Parameter(description = "Search keyword", example = "John")
            @RequestParam String keyword,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userPrincipal.getUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedDate"));
        Page<ApplicationResponse> applications = applicationService.searchApplications(keyword, user, pageable);
        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "Get recent applications", description = "Retrieves recent applications for the authenticated recruiter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recent applications retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/recent")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<ApplicationResponse>> getRecentApplications(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userPrincipal.getUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedDate"));
        Page<ApplicationResponse> applications = applicationService.getRecentApplications(user, pageable);
        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "Get old pending applications", description = "Retrieves pending applications that need attention (older than specified days)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Old pending applications retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/old-pending")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<?> getOldPendingApplications(
            @Parameter(description = "Days threshold", example = "7")
            @RequestParam(defaultValue = "7") int daysThreshold,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userPrincipal.getUser();
        // Note: This method needs to be updated to work with User instead of Company
        // For now, we'll return an empty list as a placeholder
        List<?> applications = List.of(); // applicationService.getOldPendingApplications(user, daysThreshold);
        return ResponseEntity.ok(applications);
    }

    // Company-based application management endpoints

    @Operation(summary = "Get all applications for company", description = "Retrieves all applications for jobs posted by the authenticated company")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Applications retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only companies can access their applications")
    })
    @GetMapping("/company")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Page<ApplicationResponse>> getApplicationsForCompany(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "appliedDate")
            @RequestParam(defaultValue = "appliedDate") String sortBy,
            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal CompanyPrincipal companyPrincipal) {
        
        Company company = companyPrincipal.getCompany();
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ApplicationResponse> applications = applicationService.getApplicationsForCompany(company, pageable);
        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "Get applications for a specific job for company", description = "Retrieves applications for a specific job (only if job belongs to company)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Applications retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Job not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Job doesn't belong to company")
    })
    @GetMapping("/company/job/{jobId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Page<ApplicationResponse>> getApplicationsByJobForCompany(
            @Parameter(description = "Job ID", example = "1")
            @PathVariable Long jobId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CompanyPrincipal companyPrincipal) {
        
        Company company = companyPrincipal.getCompany();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedDate"));
        Page<ApplicationResponse> applications = applicationService.getApplicationsByJob(jobId, company, pageable);
        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "Get applications by status for company", description = "Retrieves applications with a specific status for the authenticated company")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Applications retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/company/status/{status}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Page<ApplicationResponse>> getApplicationsByStatusForCompany(
            @Parameter(description = "Application status", example = "PENDING")
            @PathVariable ApplicationStatus status,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CompanyPrincipal companyPrincipal) {
        
        Company company = companyPrincipal.getCompany();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedDate"));
        Page<ApplicationResponse> applications = applicationService.getApplicationsByStatus(status, company, pageable);
        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "Get application by ID for company", description = "Retrieves a specific application by ID (only if job belongs to company)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Application not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Application doesn't belong to company")
    })
    @GetMapping("/company/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<ApplicationResponse> getApplicationByIdForCompany(
            @Parameter(description = "Application ID", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal CompanyPrincipal companyPrincipal) {
        
        Company company = companyPrincipal.getCompany();
        ApplicationResponse application = applicationService.getApplicationById(id, company);
        return ResponseEntity.ok(application);
    }

    @Operation(summary = "Update application status for company", description = "Updates the status of an application (only if job belongs to company)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status"),
        @ApiResponse(responseCode = "404", description = "Application not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Application doesn't belong to company")
    })
    @PutMapping("/company/{id}/status")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<ApplicationResponse> updateApplicationStatusForCompany(
            @Parameter(description = "Application ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ApplicationStatusUpdateRequest statusUpdateRequest,
            @AuthenticationPrincipal CompanyPrincipal companyPrincipal) {
        
        Company company = companyPrincipal.getCompany();
        ApplicationResponse updatedApplication = applicationService.updateApplicationStatus(
                id, statusUpdateRequest, company);
        return ResponseEntity.ok(updatedApplication);
    }

    @Operation(summary = "Search applications for company", description = "Search applications by keyword for the authenticated company")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/company/search")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Page<ApplicationResponse>> searchApplicationsForCompany(
            @Parameter(description = "Search keyword", example = "John")
            @RequestParam String keyword,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CompanyPrincipal companyPrincipal) {
        
        Company company = companyPrincipal.getCompany();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedDate"));
        Page<ApplicationResponse> applications = applicationService.searchApplications(keyword, company, pageable);
        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "Get recent applications for company", description = "Retrieves recent applications for the authenticated company")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recent applications retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/company/recent")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Page<ApplicationResponse>> getRecentApplicationsForCompany(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CompanyPrincipal companyPrincipal) {
        
        Company company = companyPrincipal.getCompany();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedDate"));
        Page<ApplicationResponse> applications = applicationService.getRecentApplicationsByCompany(company, pageable);
        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "Get old pending applications for company", description = "Retrieves pending applications that need attention (older than specified days)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Old pending applications retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/company/old-pending")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<?> getOldPendingApplicationsForCompany(
            @Parameter(description = "Days threshold", example = "7")
            @RequestParam(defaultValue = "7") int daysThreshold,
            @AuthenticationPrincipal CompanyPrincipal companyPrincipal) {
        
        Company company = companyPrincipal.getCompany();
        var applications = applicationService.getOldPendingApplications(company, daysThreshold);
        return ResponseEntity.ok(applications);
    }
}