package com.example.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /** Statuses that still need admin attention. Add UNRESOLVABLE here if you want it listed too. */
    public static final List<ReportStatus> OPEN_STATUSES =
            List.of(ReportStatus.PENDING, ReportStatus.UNDER_REVIEW);

    private final ReportRepository repository;

    public ReportController(ReportRepository repository) {
        this.repository = repository;
    }

    /** @param status filter by status, or null for all reports */
    public List<Report> getReports(ReportStatus status, SortOrder order) {
        return filterAndSort(repository.findAll(), status, order);
    }

    /**
     * Reports that are still open (see OPEN_STATUSES). Used by the admin "Unresolved Reports" page.
     * @param status narrow to one open status, or null for all open ones
     */
    public List<Report> getOpenReports(ReportStatus status, SortOrder order) {
        List<Report> open = repository.findAll().stream()
                .filter(r -> OPEN_STATUSES.contains(r.status()))
                .toList();
        return filterAndSort(open, status, order);
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

    public Report submitReport(int userId, String description, String address,
                               double latitude, double longitude) {
        Report candidate = Report.submit(userId, description, address, latitude, longitude);
        return repository.save(candidate);
    }

    @Transactional
    public Report submitReportWithMedia(int userId, String description, String address,
                                        double latitude, double longitude, List<String> mediaPaths) {
        Report saved = submitReport(userId, description, address, latitude, longitude);
        for (String path : mediaPaths) {
            saveReportImage(saved.reportId(), path, ImageType.ORIGINAL);
        }
        return saved;
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

    /** ORIGINAL still pictures (no videos) for the given reports, in one query, keyed by report id. */
    public Map<Integer, List<ReportImage>> getOriginalImages(List<Report> reports) {
        List<Integer> ids = reports.stream().map(Report::reportId).toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return repository.findImagesByReportIds(ids).stream()
                .filter(img -> img.imageType() == ImageType.ORIGINAL)
                .filter(img -> !img.imagePath().toLowerCase(Locale.ROOT).endsWith(".mp4"))
                .collect(Collectors.groupingBy(ReportImage::reportId));
    }

}