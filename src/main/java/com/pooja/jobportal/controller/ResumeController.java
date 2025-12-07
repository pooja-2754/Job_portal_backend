package com.pooja.jobportal.controller;

import com.pooja.jobportal.model.Resume;
import com.pooja.jobportal.model.User;
import com.pooja.jobportal.security.UserPrincipal;
import com.pooja.jobportal.service.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Tag(name = "Resume Management", description = "APIs for managing resume uploads and previews")
public class ResumeController {

    private final CloudinaryService cloudinaryService;

    // Route 1: Upload Resume
    @PostMapping("/upload")
    @PreAuthorize("hasRole('JOB_SEEKER') or hasRole('ADMIN')")
    @Operation(summary = "Upload a resume", description = "Upload a resume file (PDF, DOC, DOCX, JPG, PNG) to Cloudinary and store metadata")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resume uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or upload failed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only job seekers can upload resumes")
    })
    public ResponseEntity<Resume> uploadResume(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User user = userPrincipal.getUser();
            Resume savedResume = cloudinaryService.uploadResume(file, user);
            return ResponseEntity.ok(savedResume);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Route 2: Get Resume Info (including Preview URL)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('JOB_SEEKER') or hasRole('ADMIN')")
    @Operation(summary = "Get resume by ID", description = "Retrieve resume information including file URL and preview URL")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resume retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Can only access own resumes"),
        @ApiResponse(responseCode = "404", description = "Resume not found")
    })
    public ResponseEntity<Resume> getResume(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        return ResponseEntity.ok(cloudinaryService.getResume(id, user));
    }

    // Route 3: Get All User Resumes
    @GetMapping("/my-resumes")
    @PreAuthorize("hasRole('JOB_SEEKER') or hasRole('ADMIN')")
    @Operation(summary = "Get current user's resumes", description = "Retrieve all resumes uploaded by the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User resumes retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only job seekers can access their resumes")
    })
    public ResponseEntity<List<Resume>> getUserResumes(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        List<Resume> resumes = cloudinaryService.getUserResumes(user);
        return ResponseEntity.ok(resumes);
    }
}