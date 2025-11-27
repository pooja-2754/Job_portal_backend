package com.pooja.jobportal.model;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Salary {
    
    @Column(name = "salary_min")
    private Double min;
    
    @Column(name = "salary_max")
    private Double max;
    
    @Column(name = "currency")
    @Builder.Default
    private String currency = "USD";
    
    @Enumerated(EnumType.STRING)
    @Column(name = "salary_period")
    @Builder.Default
    private SalaryPeriod period = SalaryPeriod.YEARLY;
    
    @Builder.Default
    @Column(name = "is_negotiable")
    private Boolean isNegotiable = false;
    
    public enum SalaryPeriod {
        HOURLY,
        MONTHLY,
        YEARLY
    }
}