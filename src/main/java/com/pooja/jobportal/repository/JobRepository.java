package com.pooja.jobportal.repository;

import com.pooja.jobportal.model.Company;
import com.pooja.jobportal.model.Job;
import com.pooja.jobportal.model.JobType;
import com.pooja.jobportal.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    // Note: Recruiter-based methods have been removed as the Job entity now uses Company instead of recruiter

    // Public job search methods for job seekers
    
    /**
     * Find all active jobs with pagination
     */
    Page<Job> findByIsActiveTrue(Pageable pageable);
    
    /**
     * Find active jobs by location
     */
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    Page<Job> findActiveJobsByLocation(@Param("location") String location, Pageable pageable);
    
    /**
     * Find active jobs by keyword in title or description
     */
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Job> findActiveJobsByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Find active jobs by location and keyword
     */
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND " +
           "LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')) AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Job> findActiveJobsByLocationAndKeyword(@Param("location") String location,
                                                 @Param("keyword") String keyword,
                                                 Pageable pageable);
    
    /**
     * Find active jobs by type
     */
    Page<Job> findByIsActiveTrueAndType(JobType type, Pageable pageable);
    
    /**
     * Find active jobs by type and location
     */
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND j.type = :type AND " +
           "LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    Page<Job> findActiveJobsByTypeAndLocation(@Param("type") JobType type,
                                              @Param("location") String location,
                                              Pageable pageable);
    
    /**
     * Find active jobs by type and keyword
     */
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND j.type = :type AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Job> findActiveJobsByTypeAndKeyword(@Param("type") JobType type,
                                             @Param("keyword") String keyword,
                                             Pageable pageable);
    
    /**
     * Find active jobs by type, location, and keyword
     */
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND j.type = :type AND " +
           "LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')) AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Job> findActiveJobsByTypeAndLocationAndKeyword(@Param("type") JobType type,
                                                         @Param("location") String location,
                                                         @Param("keyword") String keyword,
                                                         Pageable pageable);
    
    /**
     * Count all active jobs
     */
    long countByIsActiveTrue();
    
    /**
     * Count jobs by company ID
     */
    long countByCompanyId(Long companyId);

    /**
     * Count total jobs posted by a company
     */
    long countByCompany(Company company);

    /**
     * Count active jobs posted by a company
     */
    long countByCompanyAndIsActiveTrue(Company company);

    /**
     * Count expired jobs posted by a company
     */
    @Query("SELECT COUNT(j) FROM Job j WHERE j.company = :company AND j.deadline < :currentDate")
    long countExpiredJobsByCompany(@Param("company") Company company, @Param("currentDate") LocalDate currentDate);

    // Admin-based methods (replacing recruiter-based methods)
    
    /**
     * Find all jobs posted by a specific admin (through their companies)
     */
    @Query("SELECT j FROM Job j WHERE j.company.admin = :admin")
    Page<Job> findByAdmin(@Param("admin") User admin, Pageable pageable);

    /**
     * Find all active jobs posted by a specific admin (through their companies)
     */
    @Query("SELECT j FROM Job j WHERE j.company.admin = :admin AND j.isActive = true")
    Page<Job> findByAdminAndIsActiveTrue(@Param("admin") User admin, Pageable pageable);

    /**
     * Find jobs by admin and job type
     */
    @Query("SELECT j FROM Job j WHERE j.company.admin = :admin AND j.type = :type")
    Page<Job> findByAdminAndType(@Param("admin") User admin, @Param("type") JobType type, Pageable pageable);

    /**
     * Find a job by ID and admin (to ensure admins can only access jobs from their companies)
     */
    @Query("SELECT j FROM Job j WHERE j.id = :id AND j.company.admin = :admin")
    Optional<Job> findByIdAndAdmin(@Param("id") Long id, @Param("admin") User admin);

    /**
     * Search jobs by title or company for a specific admin
     */
    @Query("SELECT j FROM Job j WHERE j.company.admin = :admin AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.company.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Job> searchJobsByAdmin(@Param("admin") User admin,
                                   @Param("keyword") String keyword,
                                   Pageable pageable);

    /**
     * Find jobs that need attention (deadline approaching within 7 days)
     */
    @Query("SELECT j FROM Job j WHERE j.company.admin = :admin AND j.isActive = true AND " +
           "j.deadline BETWEEN :currentDate AND :weekFromNow")
    List<Job> findJobsWithApproachingDeadline(@Param("admin") User admin,
                                              @Param("currentDate") LocalDate currentDate,
                                              @Param("weekFromNow") LocalDate weekFromNow);

    // Company-based methods (replacing recruiter-based methods)
    
    /**
     * Find all jobs posted by a specific company
     */
    Page<Job> findByCompany(Company company, Pageable pageable);

    /**
     * Find all active jobs posted by a specific company
     */
    Page<Job> findByCompanyAndIsActiveTrue(Company company, Pageable pageable);

    /**
     * Find all inactive jobs posted by a specific company
     */
    Page<Job> findByCompanyAndIsActiveFalse(Company company, Pageable pageable);

    /**
     * Find jobs by company and job type
     */
    Page<Job> findByCompanyAndType(Company company, JobType type, Pageable pageable);

    /**
     * Find jobs by company with deadline after specified date
     */
    Page<Job> findByCompanyAndDeadlineAfter(Company company, LocalDate date, Pageable pageable);

    /**
     * Find jobs by company with deadline before specified date (expired jobs)
     */
    Page<Job> findByCompanyAndDeadlineBefore(Company company, LocalDate date, Pageable pageable);


    /**
     * Find a job by ID and company (to ensure companies can only access their own jobs)
     */
    Optional<Job> findByIdAndCompany(Long id, Company company);

    /**
     * Search jobs by title or company for a specific company
     */
    @Query("SELECT j FROM Job j WHERE j.company = :company AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.company.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY j.postedDate DESC")
    Page<Job> searchJobsByCompany(@Param("company") Company company,
                                  @Param("keyword") String keyword,
                                  Pageable pageable);

    /**
     * Find recent jobs posted by a company (ordered by posted date)
     */
    @Query("SELECT j FROM Job j WHERE j.company = :company ORDER BY j.postedDate DESC")
    Page<Job> findRecentJobsByCompany(@Param("company") Company company, Pageable pageable);

    /**
     * Find jobs that need attention (deadline approaching within 7 days) for a company
     */
    @Query("SELECT j FROM Job j WHERE j.company = :company AND j.isActive = true AND " +
           "j.deadline BETWEEN :currentDate AND :weekFromNow")
    List<Job> findJobsWithApproachingDeadline(@Param("company") Company company,
                                              @Param("currentDate") LocalDate currentDate,
                                              @Param("weekFromNow") LocalDate weekFromNow);

    Page<Job> findByCompanyIdAndIsActiveTrue(Long companyId, Pageable pageable);

    long countByCompanyIdAndIsActiveTrue(Long companyId);
}