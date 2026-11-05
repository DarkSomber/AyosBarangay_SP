package com.example.model;

import java.time.LocalDateTime;

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
