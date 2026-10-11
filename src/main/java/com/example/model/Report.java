package com.example.model;

import java.time.LocalDateTime;

/**
 * A complaint submitted by a resident.
 * Table: reports
 *
 * The report only stores the submitter's userId. Private account details (email,
 * password hash) stay in the users table, so public screens can show a report
 * without ever loading the account.
 *
 * Soft delete: instead of removing the row, set deletedAt and deletionReason.
 * Queries for normal screens should only return rows where deletedAt is null.
 *
 * @param reportId       primary key, null until saved
 * @param userId         the resident who submitted it (FK to users)
 * @param description    details of the issue
 * @param status         current status
 * @param dateSubmitted  when it was submitted
 * @param updatedAt      latest change
 * @param address        readable location
 * @param latitude       -90 to 90
 * @param longitude      -180 to 180
 * @param reviewedBy     admin who reviewed it, null if not yet reviewed (FK to users)
 * @param reviewDate     when it was reviewed, null if not yet reviewed
 * @param deletedAt      null unless soft-deleted
 * @param deletionReason required when deletedAt is set
 */
public record Report(Integer reportId, int userId, String description, ReportStatus status,
                     LocalDateTime dateSubmitted, LocalDateTime updatedAt, String address,
                     double latitude, double longitude, Integer reviewedBy, LocalDateTime reviewDate,
                     LocalDateTime deletedAt, String deletionReason) {

    public Report {
        Validation.requirePositive(userId, "User id");
        description = Validation.requireText(description, "Description", 2000);
        Validation.requireNonNull(status, "Status");
        Validation.requireNonNull(dateSubmitted, "Date submitted");
        Validation.requireNonNull(updatedAt, "Updated at");
        address = Validation.requireText(address, "Address", 255);
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90.");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180.");
        }
        if (reviewedBy != null) {
            Validation.requirePositive(reviewedBy, "Reviewer id");
        }
        deletionReason = Validation.optionalText(deletionReason, "Deletion reason", 500);
        if (deletedAt != null && deletionReason == null) {
            throw new IllegalArgumentException("A deletion reason is required when a report is deleted.");
        }
    }

    /** Use this when a resident submits a new report. Starts as PENDING. */
    public static Report submit(int userId, String description, String address,
                                double latitude, double longitude) {
        LocalDateTime now = LocalDateTime.now();
        return new Report(null, userId, description, ReportStatus.PENDING, now, now,
                address, latitude, longitude, null, null, null, null);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
