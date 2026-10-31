package com.example.controller;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.model.ImageType;
import com.example.model.Report;
import com.example.model.ReportHistory;
import com.example.model.ReportImage;
import com.example.model.ReportRepository;
import com.example.model.ReportReviewRepository;
import com.example.model.ReportStatus;
import com.example.model.Resolution;
import com.example.model.SortOrder;
import com.example.model.User;
import com.example.model.UserRepository;

/**
 * Handles the logic between the view and the data: filtering, sorting, submission.
 * No UI code lives here.
 */
@Service
public class ReportController {

    /** Statuses that still need admin attention. Add UNRESOLVABLE here if you want it listed too. */
    public static final List<ReportStatus> OPEN_STATUSES =
            List.of(ReportStatus.PENDING, ReportStatus.UNDER_REVIEW);

    private static final String CANCEL_REASON = "Cancelled by resident";

    /** Statuses that are finished; the admin Archive tab lists these. */
    public static final List<ReportStatus> ARCHIVE_STATUSES =
            List.of(ReportStatus.RESOLVED, ReportStatus.UNRESOLVABLE);

    /** A report with the resident who sent it and the admin who reviewed it (either may be null). */
    public record ReportItem(Report report, User resident, User reviewer) {
        public String residentName() {
            return resident == null ? "Unknown" : resident.fullName();
        }
    }

    private final ReportRepository repository;
    private final ReportReviewRepository review;
    private final UserRepository users;

    public ReportController(ReportRepository repository, ReportReviewRepository review,
                            UserRepository users) {
        this.repository = repository;
        this.review = review;
        this.users = users;
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

    /**
     * Admin list/search, newest first.
     * @param allowed statuses the current admin tab may show
     * @param status  narrow to one status, or null for all of allowed
     * @param query   matches description, resident name/email, or the exact report id (#12 or 12); blank = no search
     */
    public List<ReportItem> searchReports(Collection<ReportStatus> allowed, ReportStatus status, String query) {
        List<Report> reports = repository.findAll().stream()
                .filter(r -> !r.isDeleted())
                .filter(r -> allowed.contains(r.status()))
                .filter(r -> status == null || r.status() == status)
                .sorted(Comparator.comparing(Report::dateSubmitted).reversed())
                .toList();

        Set<Integer> ids = new HashSet<>();
        for (Report r : reports) {
            ids.add(r.userId());
            if (r.reviewedBy() != null) {
                ids.add(r.reviewedBy());
            }
        }
        Map<Integer, User> people = users.findAllByIds(ids).stream()
                .collect(Collectors.toMap(User::userId, u -> u));

        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        String idText = q.startsWith("#") ? q.substring(1) : q;
        return reports.stream()
                .map(r -> new ReportItem(r, people.get(r.userId()),
                        r.reviewedBy() == null ? null : people.get(r.reviewedBy())))
                .filter(i -> q.isEmpty()
                        || i.report().description().toLowerCase(Locale.ROOT).contains(q)
                        || String.valueOf(i.report().reportId()).equals(idText)
                        || (i.resident() != null
                        && (i.resident().fullName().toLowerCase(Locale.ROOT).contains(q)
                        || i.resident().email().toLowerCase(Locale.ROOT).contains(q))))
                .toList();
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
    /**
     * Admin moves an open report to Under Review, Resolved or Unresolvable.
     * Resolved/Unresolvable need text (stored as the resolution); Under Review takes an optional note.
     */
    @Transactional
    public void updateStatus(Report report, User admin, ReportStatus newStatus, String text) {
        if (admin == null || !admin.isAdmin()) {
            throw new IllegalArgumentException("Only admins can change a report's status.");
        }
        if (!OPEN_STATUSES.contains(report.status())) {
            throw new IllegalArgumentException("This report is already closed.");
        }
        if (newStatus == null || newStatus == report.status() || newStatus == ReportStatus.PENDING) {
            throw new IllegalArgumentException("Choose a new status.");
        }
        String note = text == null || text.isBlank() ? null : text.trim();
        boolean closing = newStatus == ReportStatus.RESOLVED || newStatus == ReportStatus.UNRESOLVABLE;
        if (closing && note == null) {
            throw new IllegalArgumentException(newStatus == ReportStatus.RESOLVED
                    ? "Describe how the report was resolved." : "Give the reason it can't be resolved.");
        }

        int reportId = report.reportId();
        int adminId = admin.userId();
        // Built first so bad input fails before anything is written.
        ReportHistory history = ReportHistory.log(reportId, adminId, report.status(), newStatus,
                closing ? null : note);
        Resolution resolution = !closing ? null
                : newStatus == ReportStatus.RESOLVED ? Resolution.resolved(reportId, adminId, note)
                : Resolution.unresolvable(reportId, adminId, note);

        if (!review.updateStatus(reportId, report.status(), newStatus, adminId)) {
            throw new IllegalArgumentException(
                    "This report was already updated by someone else. Close this window and reopen it.");
        }
        review.saveHistory(history);
        if (resolution != null) {
            review.saveResolution(resolution);
        }
    }

    /** Resident edits the description and address of their own open report. */
    public void editReport(User resident, Report report, String description, String address) {
        requireOwnOpenReport(resident, report);
        String newDescription = requireText(description, "Description", 2000);
        String newAddress = requireText(address, "Address", 255);
        if (!repository.updateDetails(report.reportId(), resident.userId(),
                newDescription, newAddress, LocalDateTime.now())) {
            throw new IllegalArgumentException("This report can no longer be edited.");
        }
    }

    /** Resident cancels their own open report (soft delete, the row is kept). */
    public void cancelReport(User resident, Report report) {
        requireOwnOpenReport(resident, report);
        if (!repository.softDelete(report.reportId(), resident.userId(),
                CANCEL_REASON, LocalDateTime.now())) {
            throw new IllegalArgumentException("This report can no longer be cancelled.");
        }
    }

    /** True if this resident may edit or cancel the report (own, not deleted, still open). */
    public boolean canManage(User resident, Report report) {
        return resident != null && !resident.isAdmin() && report.userId() == resident.userId()
                && !report.isDeleted() && OPEN_STATUSES.contains(report.status());
    }

    private static String requireText(String value, String field, int max) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        String text = value.trim();
        if (text.length() > max) {
            throw new IllegalArgumentException(field + " must be at most " + max + " characters.");
        }
        return text;
    }

    private void requireOwnOpenReport(User resident, Report report) {
        if (resident == null || report == null || report.userId() != resident.userId()) {
            throw new IllegalArgumentException("You can only change your own reports.");
        }
        if (!OPEN_STATUSES.contains(report.status()) || report.isDeleted()) {
            throw new IllegalArgumentException(
                    "Only Pending or Under Review reports can be edited or cancelled.");
        }
    }

    public List<ReportHistory> getHistory(int reportId) {
        return review.findHistory(reportId);
    }

    public Optional<Resolution> getResolution(int reportId) {
        return review.findResolution(reportId);
    }

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