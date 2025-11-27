package com.pooja.jobportal.model;

import lombok.Getter;

@Getter
public enum CompanyVerificationStatus {
    PENDING("Pending"),
    VERIFIED("Verified"),
    REJECTED("Rejected");

    private final String displayName;

    CompanyVerificationStatus(String displayName) {
        this.displayName = displayName;
    }
}