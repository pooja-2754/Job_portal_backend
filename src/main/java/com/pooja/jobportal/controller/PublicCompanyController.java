package com.pooja.jobportal.controller;

import com.pooja.jobportal.dto.PublicJobResponse;
import com.pooja.jobportal.exception.ResourceNotFoundException;
import com.pooja.jobportal.model.Company;
import com.pooja.jobportal.model.CompanyVerificationStatus;
import com.pooja.jobportal.repository.CompanyRepository;
import com.pooja.jobportal.repository.JobRepository;
import com.pooja.jobportal.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/companies")
@RequiredArgsConstructor
public class PublicCompanyController {

    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final JobService jobService;

    @GetMapping
    public ResponseEntity<Page<Map<String, Object>>> listVerifiedCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort.Direction dir = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));

        Page<Company> companies = companyRepository.findByVerificationStatus(
                CompanyVerificationStatus.VERIFIED, pageable);

        Page<Map<String, Object>> response = companies.map(c -> toPublicMap(c, true));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCompanyProfile(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + id));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postedDate"));
        Page<PublicJobResponse> jobs = jobService.getActiveJobsByCompanyId(id, pageable);

        long totalActiveJobs = jobRepository.countByCompanyIdAndIsActiveTrue(id);

        Map<String, Object> result = toPublicMap(company, false);
        result.put("jobs", jobs);
        result.put("totalActiveJobs", totalActiveJobs);

        return ResponseEntity.ok(result);
    }

    private Map<String, Object> toPublicMap(Company c, boolean summary) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("name", c.getName());
        m.put("logoUrl", c.getLogoUrl());
        m.put("website", c.getWebsite());
        m.put("industry", c.getIndustry());
        m.put("companySize", c.getCompanySize());
        m.put("description", c.getDescription());
        m.put("activeJobCount", jobRepository.countByCompanyIdAndIsActiveTrue(c.getId()));
        if (!summary) {
            m.put("createdAt", c.getCreatedAt());
        }
        return m;
    }
}
