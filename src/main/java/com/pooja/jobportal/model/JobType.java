package com.pooja.jobportal.model;

public enum JobType {
    FULL_TIME("Full-Time"),
    PART_TIME("Part-Time"),
    REMOTE("Remote"),
    INTERNSHIP("Internship");

    private final String displayName;

    JobType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}