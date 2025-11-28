package com.pooja.jobportal.repository;

import com.pooja.jobportal.model.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    Optional<Company> findByName(String name);
    
    Optional<Company> findByEmail(String email);
    
    boolean existsByName(String name);
    
    boolean existsByEmail(String email);
    
    Page<Company> findByAdmin_Id(Long adminId, Pageable pageable);
    
    @Query("SELECT c FROM Company c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.industry) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Company> searchCompanies(@Param("keyword") String keyword, Pageable pageable);
}