package com.example.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

/**
 * Sample data for now. When you add JDBC, create JdbcReportRepository
 * implementing ReportRepository and remove @Repository from this class.
 */
@Repository
public class InMemoryReportRepository implements ReportRepository {

    private final List<Report> reports = List.of(
            sample(1, "The streetlight near the basketball court has been out for a week.",
                    "Purok 3, Main St.", LocalDateTime.of(2026, 10, 8, 9, 0), ReportStatus.PENDING),
            sample(2, "Water pools on the road every time it rains.",
                    "Purok 1, Riverside Rd.", LocalDateTime.of(2026, 10, 5, 14, 30), ReportStatus.UNDER_REVIEW),
            sample(3, "Trash has not been picked up since Monday.",
                    "Purok 5, Mabini St.", LocalDateTime.of(2026, 10, 3, 8, 15), ReportStatus.UNDER_REVIEW),
            sample(4, "Large pothole near the barangay hall entrance.",
                    "Barangay Hall Rd.", LocalDateTime.of(2026, 9, 28, 16, 45), ReportStatus.RESOLVED),
            sample(5, "A group of stray dogs gathers by the school gate.",
                    "Purok 2, School Ave.", LocalDateTime.of(2026, 9, 25, 7, 50), ReportStatus.UNRESOLVABLE));

    private static Report sample(int id, String description, String address,
                                 LocalDateTime submitted, ReportStatus status) {
        // Placeholder coordinates near Manila, one slightly different point per sample report
        double lat = 14.5995 + id * 0.0015;
        double lng = 120.9842 + id * 0.0020;
        return new Report(id, 1, description, status, submitted, submitted, address,
                lat, lng, null, null, null, null);
    }

    @Override
    public List<Report> findAll() {
        return reports;
    }
}