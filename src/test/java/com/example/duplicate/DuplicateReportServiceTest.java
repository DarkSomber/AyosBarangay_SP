package com.example.duplicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.model.Report;
import com.example.model.ReportRepository;
import com.example.model.ReportStatus;

class DuplicateReportServiceTest {

    private final ReportRepository repository = mock(ReportRepository.class);
    private final DuplicateReportService service = new DuplicateReportService(repository);
    private final LocalDateTime now = LocalDateTime.now();

    @Test
    void findsExactDescriptionWithNearbyCoordinates() {
        Report current = report(1, "Streetlight out near the court", "North Road",
                now, 14.6000, 120.9800, null);
        Report match = report(2, " STREETLIGHT out, near the court! ", "Different address",
                now.minusDays(2), 14.6005, 120.9800, null);
        when(repository.findAll()).thenReturn(List.of(match));

        List<DuplicateCandidate> candidates = service.findCandidates(current);

        assertEquals(1, candidates.size());
        assertTrue(candidates.getFirst().reason().contains("same normalized description"));
        assertTrue(candidates.getFirst().reason().contains("within 250 m"));
    }

    @Test
    void findsSimilarDescriptionAtSameAddress() {
        Report current = report(1, "Large pothole near barangay hall entrance", "Barangay Hall Rd.",
                now, 14.6000, 120.9800, null);
        Report match = report(2, "Large pothole near barangay hall", "  BARANGAY hall rd ",
                now.minusDays(1), 14.7000, 121.0000, null);
        when(repository.findAll()).thenReturn(List.of(match));

        List<DuplicateCandidate> candidates = service.findCandidates(current);

        assertEquals(1, candidates.size());
        assertTrue(candidates.getFirst().reason().contains("similar description"));
        assertTrue(candidates.getFirst().reason().contains("same normalized address"));
    }

    @Test
    void doesNotMatchUnrelatedDescriptionEvenAtSameLocation() {
        Report current = report(1, "Streetlight out near the court", "North Road",
                now, 14.6000, 120.9800, null);
        Report unrelated = report(2, "Garbage collection missed on Monday", "North Road",
                now.minusDays(1), 14.6001, 120.9800, null);
        when(repository.findAll()).thenReturn(List.of(unrelated));

        assertTrue(service.findCandidates(current).isEmpty());
    }

    @Test
    void excludesTheCurrentReport() {
        Report current = report(1, "Streetlight out near the court", "North Road",
                now, 14.6000, 120.9800, null);
        when(repository.findAll()).thenReturn(List.of(current));

        assertTrue(service.findCandidates(current).isEmpty());
    }

    @Test
    void excludesSoftDeletedReports() {
        Report current = report(1, "Streetlight out near the court", "North Road",
                now, 14.6000, 120.9800, null);
        Report deleted = report(2, "Streetlight out near the court", "North Road",
                now.minusDays(1), 14.6000, 120.9800, now.minusHours(1));
        when(repository.findAll()).thenReturn(List.of(deleted));

        assertTrue(service.findCandidates(current).isEmpty());
    }

    @Test
    void doesNotTreatZeroCoordinatesAsLocationEvidence() {
        Report current = report(1, "Streetlight out near the court", "North Road",
                now, 0, 0, null);
        Report other = report(2, "Streetlight out near the court", "South Road",
                now.minusDays(1), 0, 0, null);
        when(repository.findAll()).thenReturn(List.of(other));

        assertTrue(service.findCandidates(current).isEmpty());
    }

    @Test
    void usesMatchingAddressWhenCoordinatesAreUnavailable() {
        Report current = report(1, "Streetlight out near the court", "North Road",
                now, 0, 0, null);
        Report other = report(2, "Streetlight out near the court", " north-road ",
                now.minusDays(1), 0, 0, null);
        when(repository.findAll()).thenReturn(List.of(other));

        List<DuplicateCandidate> candidates = service.findCandidates(current);

        assertEquals(1, candidates.size());
        assertTrue(candidates.getFirst().reason().contains("same normalized address"));
        assertTrue(!candidates.getFirst().reason().contains("within 250 m"));
    }

    @Test
    void ignoresReportsOutsideTheRecentWindow() {
        Report current = report(1, "Streetlight out near the court", "North Road",
                now, 14.6000, 120.9800, null);
        Report old = report(2, "Streetlight out near the court", "North Road",
                now.minusDays(31), 14.6000, 120.9800, null);
        when(repository.findAll()).thenReturn(List.of(old));

        assertTrue(service.findCandidates(current).isEmpty());
    }

    private Report report(int id, String description, String address, LocalDateTime submitted,
                          double latitude, double longitude, LocalDateTime deletedAt) {
        return new Report(id, 1, description, ReportStatus.PENDING, submitted, submitted,
                address, latitude, longitude, null, null, deletedAt,
                deletedAt == null ? null : "Removed for test");
    }
}
