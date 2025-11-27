package com.pooja.jobportal.service;

import com.pooja.jobportal.dto.CompanyRequest;
import com.pooja.jobportal.dto.CompanyResponse;
import com.pooja.jobportal.exception.ResourceNotFoundException;
import com.pooja.jobportal.exception.UnauthorizedAccessException;
import com.pooja.jobportal.model.Company;
import com.pooja.jobportal.model.CompanyVerificationStatus;
import com.pooja.jobportal.model.User;
import com.pooja.jobportal.repository.CompanyRepository;
import com.pooja.jobportal.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;

    /**
     * Create a new company
     */
    public CompanyResponse createCompany(CompanyRequest companyRequest, User user) {
        log.info("Creating new company: {} by user: {}", companyRequest.getName(), user.getEmail());

        // Check if company with the same name already exists
        if (companyRepository.existsByName(companyRequest.getName())) {
            throw new IllegalArgumentException("Company with name '" + companyRequest.getName() + "' already exists");
        }

        Company company = Company.builder()
                .name(companyRequest.getName())
                .logoUrl(companyRequest.getLogoUrl())
                .website(companyRequest.getWebsite())
                .description(companyRequest.getDescription())
                .industry(companyRequest.getIndustry())
                .companySize(companyRequest.getCompanySize())
                .verificationStatus(CompanyVerificationStatus.PENDING)
                .ownerId(user.getId())
                .build();

        Company savedCompany = companyRepository.save(company);
        log.info("Company created successfully with ID: {}", savedCompany.getId());

        return convertToCompanyResponse(savedCompany);
    }

    /**
     * Get all companies with pagination
     */
    @Transactional(readOnly = true)
    public Page<CompanyResponse> getAllCompanies(Pageable pageable) {
        log.debug("Fetching all companies with pagination");

        Page<Company> companies = companyRepository.findAll(pageable);
        List<CompanyResponse> companyResponses = companies.getContent().stream()
                .map(this::convertToCompanyResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(companyResponses, pageable, companies.getTotalElements());
    }

    /**
     * Get company by ID
     */
    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(Long companyId) {
        log.debug("Fetching company with ID: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId));

        return convertToCompanyResponse(company);
    }

    /**
     * Update an existing company
     */
    public CompanyResponse updateCompany(Long companyId, CompanyRequest companyRequest, User user) {
        log.info("Updating company with ID: {} by user: {}", companyId, user.getEmail());

        Company existingCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId));

        // Check if user is the owner or an admin
        if (!existingCompany.getOwnerId().equals(user.getId()) && !user.getRole().name().equals("ADMIN")) {
            throw new UnauthorizedAccessException("You are not authorized to update this company");
        }

        // Check if another company with the same name already exists
        if (!existingCompany.getName().equals(companyRequest.getName()) && 
            companyRepository.existsByName(companyRequest.getName())) {
            throw new IllegalArgumentException("Company with name '" + companyRequest.getName() + "' already exists");
        }

        existingCompany.setName(companyRequest.getName());
        existingCompany.setLogoUrl(companyRequest.getLogoUrl());
        existingCompany.setWebsite(companyRequest.getWebsite());
        existingCompany.setDescription(companyRequest.getDescription());
        existingCompany.setIndustry(companyRequest.getIndustry());
        existingCompany.setCompanySize(companyRequest.getCompanySize());

        Company updatedCompany = companyRepository.save(existingCompany);
        log.info("Company updated successfully with ID: {}", updatedCompany.getId());

        return convertToCompanyResponse(updatedCompany);
    }

    /**
     * Delete a company
     */
    public void deleteCompany(Long companyId, User user) {
        log.info("Deleting company with ID: {} by user: {}", companyId, user.getEmail());

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId));

        // Check if user is the owner or an admin
        if (!company.getOwnerId().equals(user.getId()) && !user.getRole().name().equals("ADMIN")) {
            throw new UnauthorizedAccessException("You are not authorized to delete this company");
        }

        // Check if there are any jobs associated with this company
        long jobCount = jobRepository.countByCompanyId(companyId);
        if (jobCount > 0) {
            throw new IllegalStateException("Cannot delete company with associated jobs. Please delete all jobs first.");
        }

        companyRepository.delete(company);
        log.info("Company deleted successfully with ID: {}", companyId);
    }

    /**
     * Get companies owned by a specific user
     */
    @Transactional(readOnly = true)
    public Page<CompanyResponse> getCompaniesByOwner(Long userId, Pageable pageable) {
        log.debug("Fetching companies owned by user ID: {}", userId);

        Page<Company> companies = companyRepository.findByOwnerId(userId, pageable);
        List<CompanyResponse> companyResponses = companies.getContent().stream()
                .map(this::convertToCompanyResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(companyResponses, pageable, companies.getTotalElements());
    }

    /**
     * Search companies by keyword
     */
    @Transactional(readOnly = true)
    public Page<CompanyResponse> searchCompanies(String keyword, Pageable pageable) {
        log.debug("Searching companies with keyword: {}", keyword);

        Page<Company> companies = companyRepository.searchCompanies(keyword, pageable);
        List<CompanyResponse> companyResponses = companies.getContent().stream()
                .map(this::convertToCompanyResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(companyResponses, pageable, companies.getTotalElements());
    }

    /**
     * Verify a company (admin only)
     */
    public CompanyResponse verifyCompany(Long companyId, User user) {
        log.info("Verifying company with ID: {} by admin: {}", companyId, user.getEmail());

        if (!user.getRole().name().equals("ADMIN")) {
            throw new UnauthorizedAccessException("Only administrators can verify companies");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId));

        company.setVerificationStatus(CompanyVerificationStatus.VERIFIED);
        company.setVerifiedAt(LocalDateTime.now());

        Company verifiedCompany = companyRepository.save(company);
        log.info("Company verified successfully with ID: {}", verifiedCompany.getId());

        return convertToCompanyResponse(verifiedCompany);
    }

    /**
     * Reject company verification (admin only)
     */
    public CompanyResponse rejectCompanyVerification(Long companyId, User user) {
        log.info("Rejecting verification for company with ID: {} by admin: {}", companyId, user.getEmail());

        if (!user.getRole().name().equals("ADMIN")) {
            throw new UnauthorizedAccessException("Only administrators can reject company verification");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + companyId));

        company.setVerificationStatus(CompanyVerificationStatus.REJECTED);
        company.setVerifiedAt(null);

        Company rejectedCompany = companyRepository.save(company);
        log.info("Company verification rejected successfully with ID: {}", rejectedCompany.getId());

        return convertToCompanyResponse(rejectedCompany);
    }

    /**
     * Convert Company entity to CompanyResponse DTO
     */
    private CompanyResponse convertToCompanyResponse(Company company) {
        Long jobCount = jobRepository.countByCompanyId(company.getId());
        String ownerName = null;

        // Get owner name if owner ID is available
        if (company.getOwnerId() != null) {
            // This would require UserRepository, but for now we'll skip it
            // In a real implementation, you would inject UserRepository and fetch the owner
            ownerName = "Owner ID: " + company.getOwnerId();
        }

        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .logoUrl(company.getLogoUrl())
                .website(company.getWebsite())
                .description(company.getDescription())
                .industry(company.getIndustry())
                .companySize(company.getCompanySize())
                .verificationStatus(company.getVerificationStatus())
                .verifiedAt(company.getVerifiedAt())
                .ownerId(company.getOwnerId())
                .ownerName(ownerName)
                .jobCount(jobCount)
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}