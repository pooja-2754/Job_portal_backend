package com.pooja.jobportal.controller;

import com.pooja.jobportal.dto.DashboardStatsResponse;
import com.pooja.jobportal.security.CompanyPrincipal;
import com.pooja.jobportal.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/dashboard")
@RequiredArgsConstructor
@Tag(name = "Company Dashboard", description = "APIs for company dashboard statistics and analytics")
public class CompanyDashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get company dashboard statistics", description = "Retrieves comprehensive dashboard statistics for the authenticated company")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dashboard statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only companies can access their dashboard")
    })
    @GetMapping
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<DashboardStatsResponse> getCompanyDashboardStats(
            @AuthenticationPrincipal CompanyPrincipal companyPrincipal) {
        
        DashboardStatsResponse stats = dashboardService.getCompanyDashboardStats(companyPrincipal.getCompany());
        return ResponseEntity.ok(stats);
    }
}