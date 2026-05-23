package com.pooja.jobportal.repository;

import com.pooja.jobportal.model.Job;
import com.pooja.jobportal.model.SavedJob;
import com.pooja.jobportal.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    Page<SavedJob> findByUser(User user, Pageable pageable);

    boolean existsByUserAndJob(User user, Job job);

    Optional<SavedJob> findByUserAndJob(User user, Job job);

    long countByUser(User user);
}
