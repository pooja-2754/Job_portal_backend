package com.pooja.jobportal.service;

import com.pooja.jobportal.dto.AuthResponse;
import com.pooja.jobportal.dto.CompanyLoginRequest;
import com.pooja.jobportal.dto.CompanySignupRequest;
import com.pooja.jobportal.dto.TokenValidationResponse;
import com.pooja.jobportal.dto.RefreshTokenRequest;
import com.pooja.jobportal.dto.CompanyResponse;
import com.pooja.jobportal.model.Company;
import com.pooja.jobportal.repository.CompanyRepository;
import com.pooja.jobportal.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyAuthService {

    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse signup(CompanySignupRequest req) {

        if (companyRepository.existsByEmail(req.getEmail())) {
            return AuthResponse.messageOnly("Company with this email already exists");
        }

        if (companyRepository.existsByName(req.getName())) {
            return AuthResponse.messageOnly("Company with this name already exists");
        }

        Company company = Company.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .logoUrl(req.getLogoUrl())
                .website(req.getWebsite())
                .description(req.getDescription())
                .industry(req.getIndustry())
                .companySize(req.getCompanySize())
                .verificationStatus(com.pooja.jobportal.model.CompanyVerificationStatus.PENDING)
                .build();

        companyRepository.save(company);

        return AuthResponse.messageOnly("Company registered successfully");
    }

    public AuthResponse login(CompanyLoginRequest req) {

        Company company = companyRepository.findByEmail(req.getEmail())
                .orElse(null);

        if (company == null || !passwordEncoder.matches(req.getPassword(), company.getPassword())) {
            return AuthResponse.messageOnly("Invalid email or password");
        }

        String token = jwtTokenProvider.generateCompanyToken(company.getEmail());
        
        CompanyResponse companyResponse = CompanyResponse.builder()
            .id(company.getId())
            .name(company.getName())
            .email(company.getEmail())
            .logoUrl(company.getLogoUrl())
            .website(company.getWebsite())
            .description(company.getDescription())
            .industry(company.getIndustry())
            .companySize(company.getCompanySize())
            .verificationStatus(company.getVerificationStatus())
            .verifiedAt(company.getVerifiedAt())
            .jobCount(0L) // Will be calculated if needed
            .createdAt(company.getCreatedAt())
            .updatedAt(company.getUpdatedAt())
            .build();

        return AuthResponse.withCompany(token, companyResponse, "Login successful");
    }

    public TokenValidationResponse validateToken(String token) {
        try {
            // Check if token is null or empty
            if (token == null || token.trim().isEmpty()) {
                return new TokenValidationResponse(false, null, 0, "Token is missing", null);
            }

            // Remove "Bearer " prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            JwtTokenProvider.TokenValidationResult result = jwtTokenProvider.validateTokenDetailed(token);
            
            if (!result.isValid()) {
                return new TokenValidationResponse(false, null, 0, result.getMessage(), null);
            }

            // Get company details
            String email = result.getEmail();
            Company company = companyRepository.findByEmail(email).orElse(null);
            
            if (company == null) {
                return new TokenValidationResponse(false, null, 0, "Company not found", null);
            }

            return new TokenValidationResponse(
                true,
                email,
                result.getExpiration().getTime(),
                result.getMessage(),
                result.getEntityType()
            );
        } catch (Exception e) {
            // Log the exception for debugging
            System.err.println("Error in validateToken: " + e.getMessage());
            e.printStackTrace();
            return new TokenValidationResponse(false, null, 0, "Internal server error during token validation", null);
        }
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getToken();
        
        // Remove "Bearer " prefix if present
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Validate the current token with detailed validation
        JwtTokenProvider.TokenValidationResult validationResult = jwtTokenProvider.validateTokenDetailed(token);
        
        if (!validationResult.isValid()) {
            return AuthResponse.messageOnly(validationResult.getMessage());
        }

        // Extract email from validation result
        String email = validationResult.getEmail();
        
        // Check if company still exists
        Company company = companyRepository.findByEmail(email).orElse(null);
        if (company == null) {
            return AuthResponse.messageOnly("Company not found");
        }

        // Generate new token
        String newToken = jwtTokenProvider.generateCompanyToken(email);
        
        return AuthResponse.messageOnly("Token refreshed successfully");
    }
}