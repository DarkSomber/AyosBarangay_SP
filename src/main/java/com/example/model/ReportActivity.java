package com.example.model;

import java.time.LocalDateTime;

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