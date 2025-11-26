package com.pooja.jobportal.controller;

import com.pooja.jobportal.dto.JobRequest;
import com.pooja.jobportal.dto.JobResponse;
import com.pooja.jobportal.model.JobType;
import com.pooja.jobportal.model.User;
import com.pooja.jobportal.security.UserPrincipal;
import com.pooja.jobportal.service.JobService;
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
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Job Management", description = "APIs for job management by recruiters")
public class JobController {

    private final JobService jobService;

    @Operation(summary = "Create a new job", description = "Creates a new job posting for the authenticated recruiter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Job created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only recruiters can create jobs")
    })
    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody JobRequest jobRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userPrincipal.getUser();
        JobResponse jobResponse = jobService.createJob(jobRequest, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(jobResponse);
    }

    @Operation(summary = "Get all jobs for recruiter", description = "Retrieves all jobs posted by the authenticated recruiter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Jobs retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only recruiters can access their jobs")
    })
    @GetMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<JobResponse>> getJobs(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "postedDate")
            @RequestParam(defaultValue = "postedDate") String sortBy,
            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userPrincipal.getUser();
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<JobResponse> jobs = jobService.getJobsForRecruiter(user, pageable);
        return ResponseEntity.ok(jobs);
    }

    @Operation(summary = "Get active jobs for recruiter", description = "Retrieves only active jobs posted by the authenticated recruiter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active jobs retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/active")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<JobResponse>> getActiveJobs(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userPrincipal.getUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postedDate"));
        Page<JobResponse> jobs = jobService.getActiveJobsForRecruiter(user, pageable);
        return ResponseEntity.ok(jobs);
    }

    @Operation(summary = "Get job by ID", description = "Retrieves a specific job by ID (only if it belongs to the recruiter)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Job not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Job doesn't belong to recruiter")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobResponse> getJobById(
            @Parameter(description = "Job ID", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userPrincipal.getUser();
        JobResponse job = jobService.getJobById(id, user);
        return ResponseEntity.ok(job);
    }

    @Operation(summary = "Update job", description = "Updates an existing job (only if it belongs to the recruiter)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Job not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Job doesn't belong to recruiter")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobResponse> updateJob(
            @Parameter(description = "Job ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody JobRequest jobRequest,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userPrincipal.getUser();
        JobResponse updatedJob = jobService.updateJob(id, jobRequest, user);
        return ResponseEntity.ok(updatedJob);
    }

    @Operation(summary = "Delete job", description = "Deletes a job (only if it belongs to the recruiter)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Job deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Job not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Job doesn't belong to recruiter")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Void> deleteJob(
            @Parameter(description = "Job ID", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userPrincipal.getUser();
        jobService.deleteJob(id, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search jobs", description = "Search jobs by keyword for the authenticated recruiter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/search")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<JobResponse>> searchJobs(
            @Parameter(description = "Search keyword", example = "Java")
            @RequestParam String keyword,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userPrincipal.getUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postedDate"));
        Page<JobResponse> jobs = jobService.searchJobs(keyword, user, pageable);
        return ResponseEntity.ok(jobs);
    }

    @Operation(summary = "Get jobs by type", description = "Retrieves jobs of a specific type for the authenticated recruiter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Jobs retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "400", description = "Invalid job type")
    })
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<JobResponse>> getJobsByType(
            @Parameter(description = "Job type", example = "FULL_TIME")
            @PathVariable JobType type,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userPrincipal.getUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postedDate"));
        Page<JobResponse> jobs = jobService.getJobsByType(type, user, pageable);
        return ResponseEntity.ok(jobs);
    }

    @Operation(summary = "Get jobs with approaching deadlines", description = "Retrieves jobs with deadlines within 7 days")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Jobs retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/deadlines/approaching")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<List<JobResponse>> getJobsWithApproachingDeadlines(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userPrincipal.getUser();
        List<JobResponse> jobs = jobService.getJobsWithApproachingDeadlines(user);
        return ResponseEntity.ok(jobs);
    }
}