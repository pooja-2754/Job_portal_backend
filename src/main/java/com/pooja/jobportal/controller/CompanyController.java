package com.pooja.jobportal.controller;

import com.pooja.jobportal.dto.CompanyRequest;
import com.pooja.jobportal.dto.CompanyResponse;
import com.pooja.jobportal.model.Company;
import com.pooja.jobportal.model.User;
import com.pooja.jobportal.security.CompanyPrincipal;
import com.pooja.jobportal.security.UserPrincipal;
import com.pooja.jobportal.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Company Management", description = "APIs for managing companies")
public class CompanyController {

    private final CompanyService companyService;

    /**
     * Create a new company
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new company", description = "Creates a new company with the provided details")
    public ResponseEntity<CompanyResponse> createCompany(
            @Valid @RequestBody CompanyRequest companyRequest,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        User user = currentUser.getUser();
        CompanyResponse response = companyService.createCompany(companyRequest, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all companies with pagination
     */
    @GetMapping
    @Operation(summary = "Get all companies", description = "Retrieves a paginated list of all companies")
    public ResponseEntity<Page<CompanyResponse>> getAllCompanies(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CompanyResponse> response = companyService.getAllCompanies(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get company by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get company by ID", description = "Retrieves a specific company by its ID")
    public ResponseEntity<CompanyResponse> getCompanyById(
            @Parameter(description = "Company ID") @PathVariable Long id) {
        
        CompanyResponse response = companyService.getCompanyById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing company
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('COMPANY') and @companyService.isCompanyOwner(#id, authentication.principal.company.id))")
    @Operation(summary = "Update a company", description = "Updates an existing company with new details")
    public ResponseEntity<CompanyResponse> updateCompany(
            @Parameter(description = "Company ID") @PathVariable Long id,
            @Valid @RequestBody CompanyRequest companyRequest,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @AuthenticationPrincipal CompanyPrincipal companyPrincipal) {
        
        if (companyPrincipal != null) {
            // Company is updating their own profile
            Company company = companyPrincipal.getCompany();
            if (!company.getId().equals(id)) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
            }
            // For company self-update, we need a different method or modify the existing one
            CompanyResponse response = companyService.updateCompanyByOwner(id, companyRequest, company);
            return ResponseEntity.ok(response);
        } else {
            // Admin is updating company
            User user = currentUser.getUser();
            CompanyResponse response = companyService.updateCompany(id, companyRequest, user);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Delete a company
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('COMPANY') and @companyService.isCompanyOwner(#id, authentication.principal.company.id))")
    @Operation(summary = "Delete a company", description = "Deletes a company (only if no jobs are associated)")
    public ResponseEntity<Void> deleteCompany(
            @Parameter(description = "Company ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @AuthenticationPrincipal CompanyPrincipal companyPrincipal) {
        
        if (companyPrincipal != null) {
            // Company is deleting their own profile
            Company company = companyPrincipal.getCompany();
            if (!company.getId().equals(id)) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
            }
            companyService.deleteCompanyByOwner(id, company);
        } else {
            // Admin is deleting company
            User user = currentUser.getUser();
            companyService.deleteCompany(id, user);
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Get companies owned by the current user
     */
    @GetMapping("/my-companies")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get my companies", description = "Retrieves companies owned by the current user")
    public ResponseEntity<Page<CompanyResponse>> getMyCompanies(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        User user = currentUser.getUser();
        Page<CompanyResponse> response = companyService.getCompaniesByAdmin(user.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Search companies by keyword
     */
    @GetMapping("/search")
    @Operation(summary = "Search companies", description = "Searches companies by keyword in name, industry, or description")
    public ResponseEntity<Page<CompanyResponse>> searchCompanies(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CompanyResponse> response = companyService.searchCompanies(keyword, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify a company (admin only)
     */
    @PutMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verify a company", description = "Verifies a company (admin only)")
    public ResponseEntity<CompanyResponse> verifyCompany(
            @Parameter(description = "Company ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        User user = currentUser.getUser();
        CompanyResponse response = companyService.verifyCompany(id, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Reject company verification (admin only)
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject company verification", description = "Rejects company verification (admin only)")
    public ResponseEntity<CompanyResponse> rejectCompanyVerification(
            @Parameter(description = "Company ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        User user = currentUser.getUser();
        CompanyResponse response = companyService.rejectCompanyVerification(id, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current company profile (for authenticated companies)
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Get current company profile", description = "Retrieves the profile of the authenticated company")
    public ResponseEntity<CompanyResponse> getCurrentCompanyProfile(
            @AuthenticationPrincipal CompanyPrincipal companyPrincipal) {
        
        Company company = companyPrincipal.getCompany();
        CompanyResponse response = companyService.getCompanyById(company.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Update current company profile (for authenticated companies)
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(summary = "Update current company profile", description = "Updates the profile of the authenticated company")
    public ResponseEntity<CompanyResponse> updateCurrentCompanyProfile(
            @Valid @RequestBody CompanyRequest companyRequest,
            @AuthenticationPrincipal CompanyPrincipal companyPrincipal) {
        
        Company company = companyPrincipal.getCompany();
        CompanyResponse response = companyService.updateCompanyByOwner(company.getId(), companyRequest, company);
        return ResponseEntity.ok(response);
    }
}