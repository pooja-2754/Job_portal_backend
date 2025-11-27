package com.pooja.jobportal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column
    private String website;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "industry")
    private String industry;

    @Column(name = "company_size")
    private String companySize;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    private CompanyVerificationStatus verificationStatus = CompanyVerificationStatus.PENDING;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "owner_id")
    private Long ownerId;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Job> jobs;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}