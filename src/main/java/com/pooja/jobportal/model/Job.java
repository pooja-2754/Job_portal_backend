package com.pooja.jobportal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Company company;

    @Column(unique = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.PUBLISHED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobType type;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkplaceType workplaceType = WorkplaceType.ONSITE;

    @Enumerated(EnumType.STRING)
    private ExperienceLevel experienceLevel;

    @Embedded
    private Salary salary;

    @Embedded
    private Location location;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "job_skills", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    @Builder.Default
    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "apply_url")
    private String applyUrl;

    @Builder.Default
    @Column(name = "application_count")
    private Long applicationCount = 0L;

    // Rich text fields for structured job details
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String responsibilities;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @CreationTimestamp
    @Column(name = "posted_date", nullable = false, updatable = false)
    private LocalDateTime postedDate;

    private LocalDate deadline;

    // Legacy field for backward compatibility
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Computed field - not stored in database
    @Transient
    public Long getDaysUntilDeadline() {
        if (deadline == null) {
            return null;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), deadline);
    }
}