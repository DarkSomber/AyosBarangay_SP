package com.example.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.model.ImageType;
import com.example.model.Report;
import com.example.model.ReportImage;
import com.example.model.ReportRepository;
import com.example.model.ReportStatus;
import com.example.model.SortOrder;

/**
 * Handles the logic between the view and the data: filtering, sorting, submission.
 * No UI code lives here.
 */
@Service
public class ReportController {

    private final ReportRepository repository;

    public ReportController(ReportRepository repository) {
        this.repository = repository;
    }

    /** @param status filter by status, or null for all reports */
    public List<Report> getReports(ReportStatus status, SortOrder order) {
        return filterAndSort(repository.findAll(), status, order);
    }

    /** Retrieves only the reports submitted by a specific user. */
    public List<Report> getReportsForUser(int userId, ReportStatus status, SortOrder order) {
        List<Report> userReports = repository.findAll().stream()
                .filter(r -> r.userId() == userId)
                .toList();
        return filterAndSort(userReports, status, order);
    }

    private List<Report> filterAndSort(List<Report> source, ReportStatus status, SortOrder order) {
        Comparator<Report> byDate = Comparator.comparing(Report::dateSubmitted);
        if (order == SortOrder.NEWEST) {
            byDate = byDate.reversed();
        }
        return source.stream()
                .filter(r -> !r.isDeleted())
                .filter(r -> status == null || r.status() == status)
                .sorted(byDate)
                .toList();
    }

    /**
     * Submits a new report. Uses Report.submit so the submission date and initial
     * PENDING status follow the existing convention. Rejects an exact duplicate
     * (same user, description and address) among that user's non-deleted reports.
     */
    public Report submitReport(int userId, String description, String address,
                               double latitude, double longitude) {
        Report candidate = Report.submit(userId, description, address, latitude, longitude);

        boolean duplicate = repository.findAll().stream()
                .filter(r -> !r.isDeleted())
                .anyMatch(r -> r.userId() == userId
                        && r.description().equalsIgnoreCase(candidate.description())
                        && r.address().equalsIgnoreCase(candidate.address()));
        if (duplicate) {
            throw new IllegalArgumentException("A report with the same description and address already exists.");
        }

        return repository.save(candidate);
    }

    /**
     * Persists an uploaded media file against a specific report.
     */
    public void saveReportImage(int reportId, String filePath, ImageType imageType) {
        ReportImage image = ReportImage.of(reportId, filePath, imageType);
        repository.saveImage(image);
    }

    /**
     * Retrieves all media uploaded for a report.
     */
    public List<ReportImage> getImagesForReport(int reportId) {
        return repository.findImagesByReportId(reportId);
    }
}