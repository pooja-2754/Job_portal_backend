package com.pooja.jobportal.controller;

import com.pooja.jobportal.dto.AdminStatsResponse;
import com.pooja.jobportal.dto.ApplicationResponse;
import com.pooja.jobportal.dto.CompanyResponse;
import com.pooja.jobportal.model.CompanyVerificationStatus;
import com.pooja.jobportal.model.Role;
import com.pooja.jobportal.model.User;
import com.pooja.jobportal.repository.CompanyRepository;
import com.pooja.jobportal.repository.JobRepository;
import com.pooja.jobportal.repository.ApplicationRepository;
import com.pooja.jobportal.security.UserPrincipal;
import com.pooja.jobportal.service.ApplicationService;
import com.pooja.jobportal.service.CompanyService;
import com.pooja.jobportal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final CompanyService companyService;
    private final ApplicationService applicationService;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    // ── Stats ─────────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        AdminStatsResponse stats = AdminStatsResponse.builder()
                .totalUsers(userService.countUsers())
                .totalCompanies(companyRepository.count())
                .totalJobs(jobRepository.count())
                .totalApplications(applicationRepository.count())
                .pendingCompanies(companyRepository.countByVerificationStatus(CompanyVerificationStatus.PENDING))
                .verifiedCompanies(companyRepository.countByVerificationStatus(CompanyVerificationStatus.VERIFIED))
                .activeJobs(jobRepository.countByIsActiveTrue())
                .build();
        return ResponseEntity.ok(stats);
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<Page<Map<String, Object>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction dir = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        Page<User> users = userService.getAllUsers(pageable);

        Page<Map<String, Object>> response = users.map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("name", u.getName());
            m.put("email", u.getEmail());
            m.put("role", u.getRole());
            m.put("phone", u.getPhone());
            m.put("skills", u.getSkills());
            m.put("createdAt", u.getCreatedAt());
            return m;
        });
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        User u = userService.getUserById(id)
                .orElseThrow(() -> new com.pooja.jobportal.exception.ResourceNotFoundException("User not found: " + id));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("name", u.getName());
        m.put("email", u.getEmail());
        m.put("role", u.getRole());
        m.put("phone", u.getPhone());
        m.put("skills", u.getSkills());
        m.put("createdAt", u.getCreatedAt());
        m.put("updatedAt", u.getUpdatedAt());
        return ResponseEntity.ok(m);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal.getUser().getId().equals(id)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot delete your own account"));
        }
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal.getUser().getId().equals(id)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot change your own role"));
        }
        User user = userService.getUserById(id)
                .orElseThrow(() -> new com.pooja.jobportal.exception.ResourceNotFoundException("User not found: " + id));
        Role newRole = Role.valueOf(body.get("role").toUpperCase());
        user.setRole(newRole);
        userService.updateUser(user);
        return ResponseEntity.ok(Map.of("message", "Role updated", "role", newRole));
    }

    // ── Companies ─────────────────────────────────────────────────────────────

    @GetMapping("/companies")
    public ResponseEntity<Page<CompanyResponse>> getAllCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(companyService.getAllCompanies(pageable));
    }

    @GetMapping("/companies/{id}")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @PutMapping("/companies/{id}/verify")
    public ResponseEntity<CompanyResponse> verifyCompany(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(companyService.verifyCompany(id, principal.getUser()));
    }

    @PutMapping("/companies/{id}/reject")
    public ResponseEntity<CompanyResponse> rejectCompany(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(companyService.rejectCompanyVerification(id, principal.getUser()));
    }

    @DeleteMapping("/companies/{id}")
    public ResponseEntity<Map<String, String>> deleteCompany(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        companyService.deleteCompany(id, principal.getUser());
        return ResponseEntity.ok(Map.of("message", "Company deleted successfully"));
    }

    // ── Applications ──────────────────────────────────────────────────────────

    @GetMapping("/applications")
    public ResponseEntity<Page<ApplicationResponse>> getAllApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "appliedDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort.Direction dir = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        return ResponseEntity.ok(applicationService.getAllApplications(pageable));
    }
}
