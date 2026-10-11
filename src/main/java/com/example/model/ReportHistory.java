package com.example.model;

import java.time.LocalDateTime;

/**
 * One entry in a report's status log. Entries are only ever ADDED, never edited,
 * so the record is immutable and the code layer should have no update method for it.
 * Table: report_history
 *
 * @param historyId    primary key, null until saved
 * @param reportId     the report (FK to reports)
 * @param changedBy    who made the change (FK to users)
 * @param oldStatus    previous status, null for the very first entry
 * @param newStatus    the new status
 * @param changeReason optional note
 * @param changedAt    when it happened
 */
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
