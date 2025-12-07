package com.pooja.jobportal.repository;

import com.pooja.jobportal.model.Resume;
import com.pooja.jobportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUser(User user);
    Optional<Resume> findByUserAndId(User user, Long id);
}