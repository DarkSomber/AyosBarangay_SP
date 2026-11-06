package com.example.model;

public enum ReportStatus {
    PENDING("Pending"),
    UNDER_REVIEW("Under Review"),
    RESOLVED("Resolved"),
    UNRESOLVABLE("Unresolvable");

    private final String label;

    ReportStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
