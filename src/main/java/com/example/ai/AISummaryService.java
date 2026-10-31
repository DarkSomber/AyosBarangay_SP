package com.example.ai;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.controller.ReportController;
import com.example.model.AISummary;
import com.example.model.AISummaryRepository;
import com.example.model.Report;
import com.example.model.SortOrder;
import com.example.model.User;

/** Builds an AI summary of the reports submitted in a date range and saves it. */
@Service
public class AISummaryService {

    private static final int MAX_REPORTS = 50;
    private static final int MAX_DESCRIPTION = 300;

    private final ReportController reports;
    private final ReportAnalysisProvider provider;
    private final AISummaryRepository repository;

    public AISummaryService(ReportController reports, ReportAnalysisProvider provider,
                            AISummaryRepository repository) {
        this.reports = reports;
        this.provider = provider;
        this.repository = repository;
    }

    /** Blocking (calls the AI provider); run it off the UI thread. */
    public AISummary generate(User admin, LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            throw new IllegalArgumentException("Choose a valid date range.");
        }
        List<Report> inRange = reports.getReports(null, SortOrder.NEWEST).stream()
                .filter(r -> !r.dateSubmitted().toLocalDate().isBefore(start)
                        && !r.dateSubmitted().toLocalDate().isAfter(end))
                .toList();
        if (inRange.isEmpty()) {
            throw new IllegalArgumentException("No reports were submitted in that period.");
        }
        int shown = Math.min(inRange.size(), MAX_REPORTS);
        String text = "Reports in period: " + inRange.size() + " (showing the newest " + shown + ")\n"
                + inRange.stream().limit(MAX_REPORTS).map(this::line).collect(Collectors.joining("\n"));

        String content = provider.summarize(text);
        return repository.save(new AISummary(null, admin.userId(), content, inRange.size(),
                start, end, LocalDateTime.now()));
    }

    public List<AISummary> recent(int limit) {
        return repository.findRecent(limit);
    }

    private String line(Report r) {
        String description = r.description().replaceAll("\\s+", " ");
        if (description.length() > MAX_DESCRIPTION) {
            description = description.substring(0, MAX_DESCRIPTION) + "...";
        }
        return "#" + r.reportId() + " | " + r.dateSubmitted().toLocalDate() + " | " + r.status().name()
                + " | " + r.address() + " | " + description;
    }
}
