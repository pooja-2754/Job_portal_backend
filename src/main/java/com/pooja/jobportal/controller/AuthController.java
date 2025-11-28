package com.pooja.jobportal.controller;

import com.pooja.jobportal.dto.*;
import com.pooja.jobportal.service.AuthService;
import com.pooja.jobportal.service.CompanyAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user authentication")
public class AuthController {

    private final AuthService authService;
    private final CompanyAuthService companyAuthService;

    @Operation(summary = "Register a new user", description = "Creates a new user account with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Email already exists or invalid input")
    })
    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody SignupRequest request){
        return authService.signup(request);
    }

    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful, returns JWT token"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request){
        return authService.login(request);
    }

    @Operation(summary = "Validate JWT token", description = "Validates a JWT token and returns user information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token validation result"),
        @ApiResponse(responseCode = "400", description = "Invalid token format")
    })
    @PostMapping("/validate")
    public TokenValidationResponse validateToken(@RequestBody RefreshTokenRequest request){
        return authService.validateToken(request.getToken());
    }

    @Operation(summary = "Refresh JWT token", description = "Generates a new JWT token for a valid existing token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid token or user not found")
    })
    @PostMapping("/refresh")
    public AuthResponse refreshToken(@RequestBody RefreshTokenRequest request){
        return authService.refreshToken(request);
    }

    @Operation(summary = "Register a new company", description = "Creates a new company account with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Company registered successfully"),
        @ApiResponse(responseCode = "400", description = "Email already exists or invalid input")
    })
    @PostMapping("/company/signup")
    public AuthResponse companySignup(@RequestBody CompanySignupRequest request){
        return companyAuthService.signup(request);
    }

    @Operation(summary = "Company login", description = "Authenticates a company and returns a JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful, returns JWT token"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/company/login")
    public AuthResponse companyLogin(@RequestBody CompanyLoginRequest request){
        return companyAuthService.login(request);
    }

    @Operation(summary = "Refresh company JWT token", description = "Generates a new JWT token for a valid existing company token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid token or company not found")
    })
    @PostMapping("/company/refresh")
    public AuthResponse refreshCompanyToken(@RequestBody RefreshTokenRequest request){
        return companyAuthService.refreshToken(request);
    }
}
