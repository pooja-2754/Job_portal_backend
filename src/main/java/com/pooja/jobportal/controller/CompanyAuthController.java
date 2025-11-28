package com.pooja.jobportal.controller;

import com.pooja.jobportal.dto.AuthResponse;
import com.pooja.jobportal.dto.CompanyLoginRequest;
import com.pooja.jobportal.dto.CompanySignupRequest;
import com.pooja.jobportal.dto.RefreshTokenRequest;
import com.pooja.jobportal.dto.TokenValidationResponse;
import com.pooja.jobportal.service.CompanyAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Company Authentication", description = "APIs for company authentication and token management")
public class CompanyAuthController {

    private final CompanyAuthService companyAuthService;

    @Operation(summary = "Company signup", description = "Register a new company account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Company registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data or company already exists")
    })
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody CompanySignupRequest request) {
        log.info("Company signup attempt for email: {}", request.getEmail());
        AuthResponse response = companyAuthService.signup(request);
        
        if (response.getMessage().equals("Company registered successfully")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Company login", description = "Authenticate company and return JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody CompanyLoginRequest request) {
        log.info("Company login attempt for email: {}", request.getEmail());
        AuthResponse response = companyAuthService.login(request);
        
        if (response.getMessage().equals("Login successful")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @Operation(summary = "Refresh company token", description = "Refresh JWT token for company")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Company token refresh attempt");
        AuthResponse response = companyAuthService.refreshToken(request);
        
        if (response.getMessage().equals("Token refreshed successfully")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @Operation(summary = "Validate company token", description = "Validate JWT token and return token details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token is valid"),
        @ApiResponse(responseCode = "401", description = "Token is invalid or expired")
    })
    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("Company token validation attempt");
        
        if (authHeader == null || authHeader.trim().isEmpty()) {
            TokenValidationResponse response = new TokenValidationResponse(false, null, 0, "Authorization header is missing", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        TokenValidationResponse response = companyAuthService.validateToken(authHeader);
        
        if (response.isValid()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}