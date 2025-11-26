package com.pooja.jobportal.controller;

import com.pooja.jobportal.dto.PublicJobResponse;
import com.pooja.jobportal.model.JobType;
import com.pooja.jobportal.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/jobs")
@RequiredArgsConstructor
@Tag(name = "Public Job Search", description = "Public APIs for job seekers to search and view jobs")
public class PublicJobController {

    private final JobService jobService;

    @Operation(summary = "Get all active jobs", description = "Retrieves all active job postings with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active jobs retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<Page<PublicJobResponse>> getAllActiveJobs(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "postedDate")
            @RequestParam(defaultValue = "postedDate") String sortBy,
            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<PublicJobResponse> jobs = jobService.getAllActiveJobs(pageable);
        return ResponseEntity.ok(jobs);
    }

    @Operation(summary = "Get job by ID", description = "Retrieves a specific active job by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Job not found or inactive")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PublicJobResponse> getJobById(
            @Parameter(description = "Job ID", example = "1")
            @PathVariable Long id) {
        
        PublicJobResponse job = jobService.getActiveJobById(id);
        return ResponseEntity.ok(job);
    }

    @Operation(summary = "Search jobs by keyword", description = "Search active jobs by title or description")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<PublicJobResponse>> searchJobsByKeyword(
            @Parameter(description = "Search keyword", example = "Java Developer")
            @RequestParam String keyword,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "postedDate")
            @RequestParam(defaultValue = "postedDate") String sortBy,
            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<PublicJobResponse> jobs = jobService.searchJobsByKeyword(keyword, pageable);
        return ResponseEntity.ok(jobs);
    }

    @Operation(summary = "Search jobs by location", description = "Search active jobs by location")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    })
    @GetMapping("/location/{location}")
    public ResponseEntity<Page<PublicJobResponse>> searchJobsByLocation(
            @Parameter(description = "Job location", example = "New York")
            @PathVariable String location,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "postedDate")
            @RequestParam(defaultValue = "postedDate") String sortBy,
            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<PublicJobResponse> jobs = jobService.searchJobsByLocation(location, pageable);
        return ResponseEntity.ok(jobs);
    }

    @Operation(summary = "Search jobs by type", description = "Search active jobs by employment type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    })
    @GetMapping("/type/{type}")
    public ResponseEntity<Page<PublicJobResponse>> searchJobsByType(
            @Parameter(description = "Job type", example = "FULL_TIME")
            @PathVariable JobType type,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "postedDate")
            @RequestParam(defaultValue = "postedDate") String sortBy,
            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<PublicJobResponse> jobs = jobService.searchJobsByType(type, pageable);
        return ResponseEntity.ok(jobs);
    }

    @Operation(summary = "Advanced search", description = "Search active jobs with multiple filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    })
    @GetMapping("/filter")
    public ResponseEntity<Page<PublicJobResponse>> advancedSearch(
            @Parameter(description = "Search keyword (title or description)", example = "Java")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Job location", example = "New York")
            @RequestParam(required = false) String location,
            @Parameter(description = "Job type", example = "FULL_TIME")
            @RequestParam(required = false) JobType type,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "postedDate")
            @RequestParam(defaultValue = "postedDate") String sortBy,
            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<PublicJobResponse> jobs;
        
        // Determine which search method to use based on provided filters
        if (type != null && location != null && keyword != null) {
            jobs = jobService.searchJobsByTypeAndLocationAndKeyword(type, location, keyword, pageable);
        } else if (type != null && location != null) {
            jobs = jobService.searchJobsByTypeAndLocation(type, location, pageable);
        } else if (type != null && keyword != null) {
            jobs = jobService.searchJobsByTypeAndKeyword(type, keyword, pageable);
        } else if (location != null && keyword != null) {
            jobs = jobService.searchJobsByLocationAndKeyword(location, keyword, pageable);
        } else if (type != null) {
            jobs = jobService.searchJobsByType(type, pageable);
        } else if (location != null) {
            jobs = jobService.searchJobsByLocation(location, pageable);
        } else if (keyword != null) {
            jobs = jobService.searchJobsByKeyword(keyword, pageable);
        } else {
            jobs = jobService.getAllActiveJobs(pageable);
        }
        
        return ResponseEntity.ok(jobs);
    }
}