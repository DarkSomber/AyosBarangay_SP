package com.example.model;

import java.time.LocalDateTime;

public record ReportHistory(Integer historyId, int reportId, int changedBy, ReportStatus oldStatus,
                            ReportStatus newStatus, String changeReason, LocalDateTime changedAt) {

    public ReportHistory {
        Validation.requirePositive(reportId, "Report id");
        Validation.requirePositive(changedBy, "Changed by");
        Validation.requireNonNull(newStatus, "New status");
        if (oldStatus == newStatus) {
            throw new IllegalArgumentException("New status must be different from the old status.");
        }
        changeReason = Validation.optionalText(changeReason, "Change reason", 500);
        Validation.requireNonNull(changedAt, "Changed at");
    }

    public static ReportHistory log(int reportId, int changedBy, ReportStatus oldStatus,
                                    ReportStatus newStatus, String changeReason) {
        return new ReportHistory(null, reportId, changedBy, oldStatus, newStatus,
                changeReason, LocalDateTime.now());
    }
}
