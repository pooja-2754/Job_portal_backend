package com.pooja.jobportal.repository;

import com.pooja.jobportal.model.Application;
import com.pooja.jobportal.model.ApplicationStatus;
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

    /**
     * Find all applications for jobs posted by a specific recruiter
     */
    @Query("SELECT a FROM Application a WHERE a.job.recruiter = :recruiter")
    Page<Application> findByRecruiter(@Param("recruiter") User recruiter, Pageable pageable);

    /**
     * Find applications for a specific job (only if job belongs to the recruiter)
     */
    @Query("SELECT a FROM Application a WHERE a.job.id = :jobId AND a.job.recruiter = :recruiter")
    Page<Application> findByJobIdAndRecruiter(@Param("jobId") Long jobId, 
                                             @Param("recruiter") User recruiter, 
                                             Pageable pageable);

    /**
     * Find applications by status for jobs posted by a specific recruiter
     */
    @Query("SELECT a FROM Application a WHERE a.job.recruiter = :recruiter AND a.status = :status")
    Page<Application> findByRecruiterAndStatus(@Param("recruiter") User recruiter, 
                                               @Param("status") ApplicationStatus status, 
                                               Pageable pageable);

    /**
     * Find applications by status for a specific job
     */
    @Query("SELECT a FROM Application a WHERE a.job.id = :jobId AND a.job.recruiter = :recruiter AND a.status = :status")
    Page<Application> findByJobIdAndRecruiterAndStatus(@Param("jobId") Long jobId, 
                                                       @Param("recruiter") User recruiter, 
                                                       @Param("status") ApplicationStatus status, 
                                                       Pageable pageable);

    /**
     * Find a specific application by ID (only if job belongs to the recruiter)
     */
    @Query("SELECT a FROM Application a WHERE a.id = :id AND a.job.recruiter = :recruiter")
    Optional<Application> findByIdAndRecruiter(@Param("id") Long id, @Param("recruiter") User recruiter);

    /**
     * Count total applications for jobs posted by a recruiter
     */
    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.recruiter = :recruiter")
    long countByRecruiter(@Param("recruiter") User recruiter);

    /**
     * Count applications by status for jobs posted by a recruiter
     */
    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.recruiter = :recruiter AND a.status = :status")
    long countByRecruiterAndStatus(@Param("recruiter") User recruiter, @Param("status") ApplicationStatus status);

    /**
     * Count applications for a specific job
     */
    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.id = :jobId AND a.job.recruiter = :recruiter")
    long countByJobIdAndRecruiter(@Param("jobId") Long jobId, @Param("recruiter") User recruiter);

    /**
     * Count pending applications for jobs posted by a recruiter
     */
    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.recruiter = :recruiter AND a.status = 'PENDING'")
    long countPendingApplicationsByRecruiter(@Param("recruiter") User recruiter);

    /**
     * Find recent applications for jobs posted by a recruiter
     */
    @Query("SELECT a FROM Application a WHERE a.job.recruiter = :recruiter ORDER BY a.appliedDate DESC")
    Page<Application> findRecentApplicationsByRecruiter(@Param("recruiter") User recruiter, Pageable pageable);

    /**
     * Search applications by applicant name or email for jobs posted by a recruiter
     */
    @Query("SELECT a FROM Application a WHERE a.job.recruiter = :recruiter AND " +
           "(LOWER(a.applicantName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.applicantEmail) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Application> searchApplicationsByRecruiter(@Param("recruiter") User recruiter, 
                                                   @Param("keyword") String keyword, 
                                                   Pageable pageable);

    /**
     * Get application status distribution for jobs posted by a recruiter
     */
    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.job.recruiter = :recruiter GROUP BY a.status")
    List<Object[]> getApplicationStatusDistributionByRecruiter(@Param("recruiter") User recruiter);

    /**
     * Get application count per job for jobs posted by a recruiter
     */
    @Query("SELECT a.job.id, a.job.title, COUNT(a) FROM Application a WHERE a.job.recruiter = :recruiter " +
           "GROUP BY a.job.id, a.job.title ORDER BY COUNT(a) DESC")
    List<Object[]> getApplicationCountPerJobByRecruiter(@Param("recruiter") User recruiter);

    /**
     * Find applications that need attention (pending for more than specified days)
     */
    @Query("SELECT a FROM Application a WHERE a.job.recruiter = :recruiter AND a.status = 'PENDING' AND " +
           "a.appliedDate < :cutoffDate ORDER BY a.appliedDate ASC")
    List<Application> findOldPendingApplications(@Param("recruiter") User recruiter,
                                                 @Param("cutoffDate") java.time.LocalDateTime cutoffDate);

    /**
     * Check if an application already exists for a job and email
     */
    boolean existsByJobIdAndApplicantEmail(Long jobId, String applicantEmail);

    /**
     * Count applications for a specific job (public endpoint)
     */
    long countByJobId(Long jobId);
}