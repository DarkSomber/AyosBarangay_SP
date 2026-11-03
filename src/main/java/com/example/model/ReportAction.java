package com.example.model;

/** What happened to a report. Shown on the public activity timeline. */
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

    /** The action an admin's move to newStatus counts as. */
    public static ReportAction forStatus(ReportStatus newStatus) {
        return switch (newStatus) {
            case UNDER_REVIEW -> REVIEWED;
            case RESOLVED -> RESOLVED;
            case UNRESOLVABLE -> UNRESOLVABLE;
            default -> STATUS_CHANGED;
        };
    }
}