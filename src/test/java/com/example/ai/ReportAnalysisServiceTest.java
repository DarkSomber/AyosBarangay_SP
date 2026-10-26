package com.example.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.example.model.Report;
import com.example.model.ReportStatus;

class ReportAnalysisServiceTest {

    @Test
    void cachesSuccessfulAnalysisForAReport() {
        AtomicInteger calls = new AtomicInteger();
        ReportAnalysis expected = analysis();
        ReportAnalysisService service = new ReportAnalysisService(description -> {
            calls.incrementAndGet();
            return expected;
        });
        Report report = report(42, "A broken streetlight.");

        assertEquals(expected, service.analyze(report).join());
        assertEquals(expected, service.analyze(report).join());
        assertEquals(expected, service.cachedAnalysis(report).orElseThrow());
        assertEquals(1, calls.get());
    }

    @Test
    void removesFailedAnalysisSoAnAdminCanRetry() {
        AtomicInteger calls = new AtomicInteger();
        ReportAnalysisService service = new ReportAnalysisService(description -> {
            if (calls.incrementAndGet() == 1) {
                throw new ReportAnalysisException("Temporary provider error.");
            }
            return analysis();
        });
        Report report = report(43, "A blocked drain.");

        assertThrows(CompletionException.class, () -> service.analyze(report).join());
        assertEquals(analysis(), service.analyze(report).join());
        assertEquals(2, calls.get());
    }

    @Test
    void rejectsReportsWithoutAnId() {
        ReportAnalysisService service = new ReportAnalysisService(description -> analysis());
        Report unsaved = new Report(null, 1, "Description", ReportStatus.PENDING,
                LocalDateTime.now(), LocalDateTime.now(), "Address", 0, 0,
                null, null, null, null);

        assertThrows(IllegalArgumentException.class, () -> service.analyze(unsaved));
    }

    private static Report report(int id, String description) {
        LocalDateTime now = LocalDateTime.now();
        return new Report(id, 1, description, ReportStatus.PENDING, now, now,
                "Address", 0, 0, null, null, null, null);
    }

    private static ReportAnalysis analysis() {
        return new ReportAnalysis(ReportUrgency.LOW, ReportCategory.INFRASTRUCTURE,
                "A streetlight is out.", "Inspect the streetlight.", false,
                "No immediate danger is stated.", "No specific indicators were provided.");
    }
}
