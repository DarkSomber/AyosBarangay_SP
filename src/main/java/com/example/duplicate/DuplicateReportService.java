package com.example.duplicate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.model.Report;
import com.example.model.ReportRepository;

/**
 * Finds advisory duplicate candidates from reports submitted within the last
 * 30 days. A match requires a high description similarity and either a matching
 * normalized address or coordinates within 250 metres.
 */
@Service
public class DuplicateReportService {

    static final int RECENT_WINDOW_DAYS = 30;
    static final double DESCRIPTION_SIMILARITY_THRESHOLD = 0.75;
    static final double LOCATION_RADIUS_METRES = 250;

    private final ReportRepository repository;

    public DuplicateReportService(ReportRepository repository) {
        this.repository = repository;
    }

    public List<DuplicateCandidate> findCandidates(Report report) {
        LocalDateTime start = report.dateSubmitted().minusDays(RECENT_WINDOW_DAYS);
        LocalDateTime end = report.dateSubmitted().plusDays(RECENT_WINDOW_DAYS);
        String description = normalize(report.description());
        String address = normalize(report.address());
        Set<String> words = words(description);

        List<DuplicateCandidate> candidates = new ArrayList<>();
        for (Report other : repository.findAll()) {
            if (other.isDeleted() || isSameReport(report, other)
                    || other.dateSubmitted().isBefore(start) || other.dateSubmitted().isAfter(end)) {
                continue;
            }

            String otherDescription = normalize(other.description());
            boolean exactDescription = description.equals(otherDescription);
            double similarity = exactDescription ? 1.0 : jaccard(words, words(otherDescription));
            if (!exactDescription && similarity < DESCRIPTION_SIMILARITY_THRESHOLD) {
                continue;
            }

            List<String> evidence = new ArrayList<>();
            evidence.add(exactDescription ? "same normalized description"
                    : "similar description (" + Math.round(similarity * 100) + "% word overlap)");

            boolean sameAddress = !address.isEmpty() && address.equals(normalize(other.address()));
            double distance = distanceMetres(report, other);
            boolean nearby = distance >= 0 && distance <= LOCATION_RADIUS_METRES;
            if (!sameAddress && !nearby) {
                continue;
            }
            if (sameAddress) {
                evidence.add("same normalized address");
            }
            if (nearby) {
                evidence.add("within 250 m");
            }
            candidates.add(new DuplicateCandidate(other, String.join("; ", evidence)));
        }

        candidates.sort(Comparator.comparing((DuplicateCandidate candidate) ->
                candidate.report().dateSubmitted()).reversed());
        return List.copyOf(candidates);
    }

    private boolean isSameReport(Report first, Report second) {
        return first.reportId() != null && Objects.equals(first.reportId(), second.reportId());
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private Set<String> words(String normalized) {
        if (normalized.isEmpty()) {
            return Set.of();
        }
        return List.of(normalized.split(" ")).stream().collect(Collectors.toSet());
    }

    private double jaccard(Set<String> first, Set<String> second) {
        if (first.isEmpty() || second.isEmpty()) {
            return 0;
        }
        long intersection = first.stream().filter(second::contains).count();
        long union = first.size() + second.size() - intersection;
        return (double) intersection / union;
    }

    private double distanceMetres(Report first, Report second) {
        if (coordinatesUnavailable(first) || coordinatesUnavailable(second)) {
            return -1;
        }
        double lat1 = Math.toRadians(first.latitude());
        double lat2 = Math.toRadians(second.latitude());
        double deltaLat = lat2 - lat1;
        double deltaLon = Math.toRadians(second.longitude() - first.longitude());
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        return 6_371_000 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private boolean coordinatesUnavailable(Report report) {
        // The current model uses primitive coordinates; (0, 0) is its missing-value sentinel.
        return report.latitude() == 0 && report.longitude() == 0;
    }
}
