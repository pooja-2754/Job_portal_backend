package com.pooja.jobportal.controller;

import com.pooja.jobportal.dto.ChangePasswordRequest;
import com.pooja.jobportal.dto.UserProfileUpdateRequest;
import com.pooja.jobportal.exception.ResourceNotFoundException;
import com.pooja.jobportal.model.Job;
import com.pooja.jobportal.model.SavedJob;
import com.pooja.jobportal.model.User;
import com.pooja.jobportal.repository.JobRepository;
import com.pooja.jobportal.repository.SavedJobRepository;
import com.pooja.jobportal.security.UserPrincipal;
import com.pooja.jobportal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(toMap(principal.getUser()));
    }

    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> updateMyProfile(
            @Valid @RequestBody UserProfileUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        User user = userService.getUserById(principal.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getSkills() != null) {
            user.setSkills(request.getSkills());
        }

        User saved = userService.updateUser(user);
        return ResponseEntity.ok(toMap(saved));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        User user = userService.getUserById(principal.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Current password is incorrect"));
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.updateUser(user);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    // ── Saved jobs ────────────────────────────────────────────────────────────

    @GetMapping("/me/saved-jobs")
    public ResponseEntity<Page<Map<String, Object>>> getSavedJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "savedAt"));
        Page<SavedJob> saved = savedJobRepository.findByUser(principal.getUser(), pageable);

        Page<Map<String, Object>> response = saved.map(sj -> {
            Job j = sj.getJob();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("savedJobId", sj.getId());
            m.put("savedAt", sj.getSavedAt());
            m.put("jobId", j.getId());
            m.put("jobTitle", j.getTitle());
            m.put("jobActive", j.getIsActive());
            m.put("companyName", j.getCompany() != null ? j.getCompany().getName() : null);
            m.put("companyLogo", j.getCompany() != null ? j.getCompany().getLogoUrl() : null);
            m.put("location", j.getLocation() != null ? j.getLocation().getCity() : null);
            m.put("type", j.getType());
            m.put("workplaceType", j.getWorkplaceType());
            m.put("experienceLevel", j.getExperienceLevel());
            m.put("deadline", j.getDeadline());
            return m;
        });
        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/saved-jobs/{jobId}")
    public ResponseEntity<Map<String, Object>> saveJob(
            @PathVariable Long jobId,
            @AuthenticationPrincipal UserPrincipal principal) {

        User user = principal.getUser();
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        if (savedJobRepository.existsByUserAndJob(user, job)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Job already saved"));
        }

        SavedJob saved = savedJobRepository.save(SavedJob.builder().user(user).job(job).build());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Job saved", "savedJobId", saved.getId(), "jobId", jobId));
    }

    @DeleteMapping("/me/saved-jobs/{jobId}")
    public ResponseEntity<Map<String, String>> unsaveJob(
            @PathVariable Long jobId,
            @AuthenticationPrincipal UserPrincipal principal) {

        User user = principal.getUser();
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        SavedJob saved = savedJobRepository.findByUserAndJob(user, job)
                .orElseThrow(() -> new ResourceNotFoundException("Saved job not found"));

        savedJobRepository.delete(saved);
        return ResponseEntity.ok(Map.of("message", "Job removed from saved list"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Map<String, Object> toMap(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("name", u.getName());
        m.put("email", u.getEmail());
        m.put("role", u.getRole());
        m.put("phone", u.getPhone());
        m.put("skills", u.getSkills());
        m.put("hasPrimaryResume", u.getPrimaryResume() != null);
        m.put("savedJobCount", savedJobRepository.countByUser(u));
        m.put("createdAt", u.getCreatedAt());
        m.put("updatedAt", u.getUpdatedAt());
        return m;
    }
}
