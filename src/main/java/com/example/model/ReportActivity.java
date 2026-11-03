package com.example.model;

import java.time.LocalDateTime;

/**
 * One public timeline entry for a report. Entries are only ever ADDED (no update or delete).
 * Table: report_activities. Everything here is safe to show publicly: remarks must never hold
 * internal notes or personal details, and performedBy is only shown by name to admins.
 *
 * @param performedBy admin who did it; null for resident or system actions
 * @param remarks     optional public explanation (e.g. how a report was resolved)
 */
public record ReportActivity(Integer activityId, int reportId, ReportAction action, ReportStatus oldStatus,
                             ReportStatus newStatus, Integer performedBy, String remarks,
                             LocalDateTime createdAt) {

    public ReportActivity {
        Validation.requirePositive(reportId, "Report id");
        Validation.requireNonNull(action, "Action");
        if (performedBy != null) {
            Validation.requirePositive(performedBy, "Performed by");
        }
        remarks = Validation.optionalText(remarks, "Remarks", 2000);
        Validation.requireNonNull(createdAt, "Created at");
    }

    public static ReportActivity log(int reportId, ReportAction action, ReportStatus oldStatus,
                                     ReportStatus newStatus, Integer performedBy, String remarks) {
        return new ReportActivity(null, reportId, action, oldStatus, newStatus, performedBy, remarks,
                LocalDateTime.now());
    }
}