package com.pooja.jobportal.model;

public enum ApplicationStatus {
    PENDING("Pending"),
    UNDER_REVIEW("Under Review"),
    SHORTLISTED("Shortlisted"),
    REJECTED("Rejected"),
    ACCEPTED("Accepted");

    private final String displayName;

    ApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}