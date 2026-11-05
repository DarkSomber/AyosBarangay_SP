package com.example.model;

public enum ReportAction {
    SUBMITTED("Report submitted"),
    REVIEWED("Report reviewed"),
    STATUS_CHANGED("Status changed"),
    RESOLVED("Report resolved"),
    UNRESOLVABLE("Report marked unresolvable"),
    CANCELLED("Report cancelled");

    private final String label;

    ReportAction(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static ReportAction forStatus(ReportStatus newStatus) {
        return switch (newStatus) {
            case UNDER_REVIEW -> REVIEWED;
            case RESOLVED -> RESOLVED;
            case UNRESOLVABLE -> UNRESOLVABLE;
            default -> STATUS_CHANGED;
        };
    }
}