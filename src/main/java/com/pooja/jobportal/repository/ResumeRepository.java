package com.pooja.jobportal.repository;

import com.pooja.jobportal.model.Resume;
import com.pooja.jobportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUser(User user);
    Optional<Resume> findByUserAndId(User user, Long id);
    
    // Find resumes that are set as primary for users
    @Query("SELECT r FROM Resume r WHERE r.id IN (SELECT u.primaryResume.id FROM User u WHERE u.primaryResume IS NOT NULL)")
    List<Resume> findPrimaryResumes();
}