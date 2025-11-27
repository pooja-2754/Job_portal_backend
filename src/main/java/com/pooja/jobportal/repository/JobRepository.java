package com.pooja.jobportal.repository;

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

    /**
     * Find all jobs posted by a specific recruiter
     */
    Page<Job> findByRecruiter(User recruiter, Pageable pageable);

    /**
     * Find all active jobs posted by a specific recruiter
     */
    Page<Job> findByRecruiterAndIsActiveTrue(User recruiter, Pageable pageable);

    /**
     * Find all inactive jobs posted by a specific recruiter
     */
    Page<Job> findByRecruiterAndIsActiveFalse(User recruiter, Pageable pageable);

    /**
     * Find jobs by recruiter and job type
     */
    Page<Job> findByRecruiterAndType(User recruiter, JobType type, Pageable pageable);

    /**
     * Find jobs by recruiter with deadline after the specified date
     */
    Page<Job> findByRecruiterAndDeadlineAfter(User recruiter, LocalDate date, Pageable pageable);

    /**
     * Find jobs by recruiter with deadline before the specified date (expired jobs)
     */
    Page<Job> findByRecruiterAndDeadlineBefore(User recruiter, LocalDate date, Pageable pageable);

    /**
     * Count total jobs posted by a recruiter
     */
    long countByRecruiter(User recruiter);

    /**
     * Count active jobs posted by a recruiter
     */
    long countByRecruiterAndIsActiveTrue(User recruiter);

    /**
     * Count expired jobs posted by a recruiter
     */
    @Query("SELECT COUNT(j) FROM Job j WHERE j.recruiter = :recruiter AND j.deadline < :currentDate")
    long countExpiredJobsByRecruiter(@Param("recruiter") User recruiter, @Param("currentDate") LocalDate currentDate);

    /**
     * Find a job by ID and recruiter (to ensure recruiters can only access their own jobs)
     */
    Optional<Job> findByIdAndRecruiter(Long id, User recruiter);

    /**
     * Search jobs by title or company for a specific recruiter
     */
    @Query("SELECT j FROM Job j WHERE j.recruiter = :recruiter AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.company) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Job> searchJobsByRecruiter(@Param("recruiter") User recruiter, 
                                   @Param("keyword") String keyword, 
                                   Pageable pageable);

    /**
     * Find recent jobs posted by a recruiter (ordered by posted date)
     */
    @Query("SELECT j FROM Job j WHERE j.recruiter = :recruiter ORDER BY j.postedDate DESC")
    Page<Job> findRecentJobsByRecruiter(@Param("recruiter") User recruiter, Pageable pageable);

    /**
     * Find jobs that need attention (deadline approaching within 7 days)
     */
    @Query("SELECT j FROM Job j WHERE j.recruiter = :recruiter AND j.isActive = true AND " +
           "j.deadline BETWEEN :currentDate AND :weekFromNow")
    List<Job> findJobsWithApproachingDeadline(@Param("recruiter") User recruiter,
                                              @Param("currentDate") LocalDate currentDate,
                                              @Param("weekFromNow") LocalDate weekFromNow);

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
}