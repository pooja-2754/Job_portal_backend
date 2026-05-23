package com.pooja.jobportal.repository;

import com.pooja.jobportal.model.Application;
import com.pooja.jobportal.model.ApplicationStatus;
import com.pooja.jobportal.model.Company;
import com.pooja.jobportal.model.Job;
import com.pooja.jobportal.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByJobIdAndApplicantEmail(Long jobId, String applicantEmail);
    
    /**
     * Check if an application already exists for a job and candidate
     */
    boolean existsByJobAndCandidate(Job job, User candidate);
    
    /**
     * Find application by job and candidate
     */
    Optional<Application> findByJobAndCandidate(Job job, User candidate);

    /**
     * Count applications for a specific job (public endpoint)
     */
    long countByJobId(Long jobId);

    // Company-based methods (replacing recruiter-based methods)
    
    /**
     * Find all applications for jobs posted by a specific company
     */
    @Query("SELECT a FROM Application a WHERE a.job.company = :company")
    Page<Application> findByCompany(@Param("company") Company company, Pageable pageable);

    /**
     * Find applications for a specific job (only if job belongs to company)
     */
    @Query("SELECT a FROM Application a WHERE a.job.id = :jobId AND a.job.company = :company")
    Page<Application> findByJobIdAndCompany(@Param("jobId") Long jobId,
                                          @Param("company") Company company,
                                          Pageable pageable);

    /**
     * Find applications by status for jobs posted by a specific company
     */
    @Query("SELECT a FROM Application a WHERE a.job.company = :company AND a.status = :status")
    Page<Application> findByCompanyAndStatus(@Param("company") Company company,
                                         @Param("status") ApplicationStatus status,
                                         Pageable pageable);

    /**
     * Count total applications for jobs posted by a company
     */
    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.company = :company")
    long countByCompany(@Param("company") Company company);

    /**
     * Count applications by status for jobs posted by a company
     */
    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.company = :company AND a.status = :status")
    long countByCompanyAndStatus(@Param("company") Company company, @Param("status") ApplicationStatus status);

    /**
     * Count pending applications for jobs posted by a company
     */
    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.company = :company AND a.status = 'PENDING'")
    long countPendingApplicationsByCompany(@Param("company") Company company);

    /**
     * Get application status distribution for jobs posted by a company
     */
    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.job.company = :company GROUP BY a.status")
    List<Object[]> getApplicationStatusDistributionByCompany(@Param("company") Company company);

    /**
     * Get application count per job for jobs posted by a company
     */
    @Query("SELECT a.job.id, a.job.title, COUNT(a) FROM Application a WHERE a.job.company = :company " +
           "GROUP BY a.job.id, a.job.title ORDER BY COUNT(a) DESC")
    List<Object[]> getApplicationCountPerJobByCompany(@Param("company") Company company);

    /**
     * Find recent applications for jobs posted by a company
     */
    @Query("SELECT a FROM Application a WHERE a.job.company = :company ORDER BY a.appliedDate DESC")
    Page<Application> findRecentApplicationsByCompany(@Param("company") Company company, Pageable pageable);

    /**
     * Find applications that need attention (pending for more than specified days) for a company
     */
    @Query("SELECT a FROM Application a WHERE a.job.company = :company AND a.status = 'PENDING' AND " +
           "a.appliedDate < :cutoffDate ORDER BY a.appliedDate ASC")
    List<Application> findOldPendingApplicationsByCompany(@Param("company") Company company,
                                                       @Param("cutoffDate") java.time.LocalDateTime cutoffDate);

    /**
     * Find a specific application by ID (only if job belongs to company)
     */
    @Query("SELECT a FROM Application a WHERE a.id = :id AND a.job.company = :company")
    Optional<Application> findByIdAndCompany(@Param("id") Long id, @Param("company") Company company);

    /**
     * Update application status for company-owned job
     */
    @Query("SELECT a FROM Application a WHERE a.id = :id AND a.job.company = :company")
    Optional<Application> findByIdAndCompanyForStatusUpdate(@Param("id") Long id, @Param("company") Company company);

    /**
     * Search applications by applicant name or email for jobs posted by a company
     */
    @Query("SELECT a FROM Application a WHERE a.job.company = :company AND " +
           "(LOWER(a.applicantName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.applicantEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY a.appliedDate DESC")
    Page<Application> searchApplicationsByCompany(@Param("company") Company company,
                                               @Param("keyword") String keyword,
                                               Pageable pageable);
    
    /**
     * Find all applications by a specific candidate
     */
    @Query("SELECT a FROM Application a WHERE a.candidate = :candidate")
    Page<Application> findByCandidate(@Param("candidate") User candidate, Pageable pageable);
    
    // Candidate-specific methods for job seeker dashboard
    
    /**
     * Count total applications for a specific candidate
     */
    @Query("SELECT COUNT(a) FROM Application a WHERE a.candidate = :candidate")
    long countByCandidate(@Param("candidate") User candidate);
    
    /**
     * Count applications by status for a specific candidate
     */
    @Query("SELECT COUNT(a) FROM Application a WHERE a.candidate = :candidate AND a.status = :status")
    long countByCandidateAndStatus(@Param("candidate") User candidate, @Param("status") ApplicationStatus status);
    
    /**
     * Get application status distribution for a candidate
     */
    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.candidate = :candidate GROUP BY a.status")
    List<Object[]> getApplicationStatusDistributionByCandidate(@Param("candidate") User candidate);
    
    /**
     * Find recent applications for a candidate
     */
    @Query("SELECT a FROM Application a WHERE a.candidate = :candidate ORDER BY a.appliedDate DESC")
    Page<Application> findRecentApplicationsByCandidate(@Param("candidate") User candidate, Pageable pageable);
}