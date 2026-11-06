package com.example.model;

import java.time.LocalDateTime;

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
