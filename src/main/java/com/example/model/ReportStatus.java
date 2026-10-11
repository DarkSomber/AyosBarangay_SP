package com.example.model;

/**
 * Where a report is in its life cycle. The database stores the enum name
 * (for example UNDER_REVIEW); getLabel() is the text shown on screen.
 * To add a status later, add it here and in the CHECK/ENUM list in schema.sql.
 */
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
