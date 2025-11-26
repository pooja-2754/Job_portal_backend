package com.pooja.jobportal.controller;

import com.pooja.jobportal.dto.DashboardStatsResponse;
import com.pooja.jobportal.model.Role;
import com.pooja.jobportal.model.User;
import com.pooja.jobportal.security.UserPrincipal;
import com.pooja.jobportal.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "APIs for recruiter dashboard statistics")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get dashboard statistics", description = "Retrieves comprehensive dashboard statistics for the authenticated recruiter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dashboard statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only recruiters can access dashboard")
    })
    @GetMapping("/stats")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userPrincipal.getUser();
        
        // Double-check role for additional security
        if (user.getRole() != Role.RECRUITER) {
            return ResponseEntity.status(403).build();
        }
        
        DashboardStatsResponse stats = dashboardService.getDashboardStats(user);
        return ResponseEntity.ok(stats);
    }
}