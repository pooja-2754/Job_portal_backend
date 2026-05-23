package com.pooja.jobportal.service;

import com.pooja.jobportal.dto.CompanyRequest;
import com.pooja.jobportal.dto.CompanyResponse;
import com.pooja.jobportal.exception.ResourceNotFoundException;
import com.pooja.jobportal.exception.UnauthorizedAccessException;
import com.pooja.jobportal.model.Company;
import com.pooja.jobportal.model.CompanyVerificationStatus;
import com.pooja.jobportal.model.Role;
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
                .admin(user)
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

        // Enhanced security check: Verify ownership or admin role
        if (!isUserAuthorizedForCompany(existingCompany, user)) {
            Long adminId = existingCompany.getAdmin() != null ? existingCompany.getAdmin().getId() : null;
            throw new UnauthorizedAccessException("You are not authorized to update this company. " +
                "Company ID: " + companyId + " is owned by admin ID: " + adminId +
                ", but your user ID is: " + user.getId());
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

        // Enhanced security check: Verify ownership or admin role
        if (!isUserAuthorizedForCompany(company, user)) {
            Long adminId = company.getAdmin() != null ? company.getAdmin().getId() : null;
            throw new UnauthorizedAccessException("You are not authorized to delete this company. " +
                "Company ID: " + companyId + " is owned by admin ID: " + adminId +
                ", but your user ID is: " + user.getId());
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
    public Page<CompanyResponse> getCompaniesByAdmin(Long adminId, Pageable pageable) {
        log.debug("Fetching companies owned by admin ID: {}", adminId);

        Page<Company> companies = companyRepository.findByAdmin_Id(adminId, pageable);
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

        // Enhanced security check: Verify admin role using Role enum
        if (!user.getRole().equals(Role.ADMIN)) {
            throw new UnauthorizedAccessException("Only administrators can verify companies. User role: " + user.getRole());
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

        // Enhanced security check: Verify admin role using Role enum
        if (!user.getRole().equals(Role.ADMIN)) {
            throw new UnauthorizedAccessException("Only administrators can reject company verification. User role: " + user.getRole());
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
     * Helper method to check if user is authorized to access a company
     * @param company The company to check
     * @param user The user to verify
     * @return true if user is authorized (owner or admin), false otherwise
     */
    private boolean isUserAuthorizedForCompany(Company company, User user) {
        // Admin can access any company
        if (user.getRole().equals(Role.ADMIN)) {
            return true;
        }
        
        // Company admin can access their own company
        if (company.getAdmin() != null && company.getAdmin().getId().equals(user.getId())) {
            return true;
        }
        
        // All other cases are unauthorized
        return false;
    }

    /**
     * Convert Company entity to CompanyResponse DTO
     */
    private CompanyResponse convertToCompanyResponse(Company company) {
        Long jobCount = jobRepository.countByCompanyId(company.getId());
        String adminName = null;

        // Get admin name if admin is available
        if (company.getAdmin() != null) {
            adminName = company.getAdmin().getName();
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
                .adminId(company.getAdmin() != null ? company.getAdmin().getId() : null)
                .adminName(adminName)
                .jobCount(jobCount)
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }

    /**
     * Update a company by the company owner (self-update)
     */
    public CompanyResponse updateCompanyByOwner(Long companyId, CompanyRequest companyRequest, Company company) {
        log.info("Updating company with ID: {} by owner: {}", companyId, company.getName());

        // Verify that the company ID matches the authenticated company
        if (!company.getId().equals(companyId)) {
            throw new UnauthorizedAccessException("You can only update your own company profile");
        }

        // Check if another company with the same name already exists
        if (!company.getName().equals(companyRequest.getName()) &&
            companyRepository.existsByName(companyRequest.getName())) {
            throw new IllegalArgumentException("Company with name '" + companyRequest.getName() + "' already exists");
        }

        // Update company fields (excluding sensitive fields that shouldn't be changed by owner)
        company.setName(companyRequest.getName());
        company.setLogoUrl(companyRequest.getLogoUrl());
        company.setWebsite(companyRequest.getWebsite());
        company.setDescription(companyRequest.getDescription());
        company.setIndustry(companyRequest.getIndustry());
        company.setCompanySize(companyRequest.getCompanySize());

        Company updatedCompany = companyRepository.save(company);
        log.info("Company updated successfully with ID: {}", updatedCompany.getId());

        return convertToCompanyResponse(updatedCompany);
    }

    /**
     * Delete a company by the company owner (self-delete)
     */
    public void deleteCompanyByOwner(Long companyId, Company company) {
        log.info("Deleting company with ID: {} by owner: {}", companyId, company.getName());

        // Verify that the company ID matches the authenticated company
        if (!company.getId().equals(companyId)) {
            throw new UnauthorizedAccessException("You can only delete your own company profile");
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
     * Check if the authenticated company is the owner of the specified company
     */
    public boolean isCompanyOwner(Long companyId, Long authenticatedCompanyId) {
        return companyId.equals(authenticatedCompanyId);
    }
}