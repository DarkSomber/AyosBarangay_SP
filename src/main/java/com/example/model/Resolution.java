package com.example.model;

import java.time.LocalDateTime;

/**
 * An admin's final outcome for a report: fixed, or cannot be fixed.
 * Table: resolutions
 *
 * Rules:
 *  - Resolved outcome: resolutionDescription is required, unresolvableReason stays null.
 *  - Unresolvable outcome: unresolvableReason is required.
 * A report may have more than one Resolution row (for example if it is reopened later),
 * so the newest one is the current outcome.
 *
 * This class only checks the data. Whether the person is really an admin must be
 * checked in the controller/service layer.
 *
 * @param resolutionId          primary key, null until saved
 * @param reportId              the report (FK to reports)
 * @param adminId               the admin responsible (FK to users)
 * @param resolutionDescription what was done to fix the issue
 * @param resolutionDate        when the outcome was recorded
 * @param unresolvableReason    why it can't be fixed, null for resolved reports
 */
public record Resolution(Integer resolutionId, int reportId, int adminId, String resolutionDescription,
                         LocalDateTime resolutionDate, String unresolvableReason) {

    public Resolution {
        Validation.requirePositive(reportId, "Report id");
        Validation.requirePositive(adminId, "Admin id");
        Validation.requireNonNull(resolutionDate, "Resolution date");
        resolutionDescription = Validation.optionalText(resolutionDescription, "Resolution description", 2000);
        unresolvableReason = Validation.optionalText(unresolvableReason, "Unresolvable reason", 2000);

        if (unresolvableReason == null && resolutionDescription == null) {
            throw new IllegalArgumentException(
                    "Provide a resolution description, or an unresolvable reason.");
        }
        if (unresolvableReason != null && resolutionDescription != null) {
            throw new IllegalArgumentException(
                    "A report is either resolved or unresolvable, not both.");
        }
    }

    public static Resolution resolved(int reportId, int adminId, String description) {
        return new Resolution(null, reportId, adminId, description, LocalDateTime.now(), null);
    }

    public static Resolution unresolvable(int reportId, int adminId, String reason) {
        return new Resolution(null, reportId, adminId, null, LocalDateTime.now(), reason);
    }

    public boolean isUnresolvable() {
        return unresolvableReason != null;
    }
}
