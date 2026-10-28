package com.example.ai;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RejectedExecutionException;

import org.springframework.stereotype.Service;

import com.example.model.Report;

@Service
public class ReportAnalysisService {

    private final ReportAnalysisProvider provider;
    private final ConcurrentMap<AnalysisKey, CompletableFuture<ReportAnalysis>> analyses =
            new ConcurrentHashMap<>();

    public ReportAnalysisService(ReportAnalysisProvider provider) {
        this.provider = provider;
    }

    public CompletableFuture<ReportAnalysis> analyze(Report report) {
        AnalysisKey key = keyFor(report);
        CompletableFuture<ReportAnalysis> analysis = new CompletableFuture<>();
        CompletableFuture<ReportAnalysis> existing = analyses.putIfAbsent(key, analysis);
        if (existing != null) {
            return existing;
        }

        try {
            CompletableFuture.runAsync(() -> {
                try {
                    analysis.complete(provider.analyze(report.description()));
                } catch (RuntimeException failure) {
                    analyses.remove(key, analysis);
                    analysis.completeExceptionally(failure);
                }
            });
        } catch (RejectedExecutionException failure) {
            analyses.remove(key, analysis);
            analysis.completeExceptionally(new ReportAnalysisException(
                    "Analysis could not be scheduled. Please try again.", failure));
        }
        return analysis;
    }

    public Optional<ReportAnalysis> cachedAnalysis(Report report) {
        CompletableFuture<ReportAnalysis> analysis = analyses.get(keyFor(report));
        if (analysis == null || !analysis.isDone() || analysis.isCompletedExceptionally()) {
            return Optional.empty();
        }
        return Optional.ofNullable(analysis.getNow(null));
    }

    private AnalysisKey keyFor(Report report) {
        if (report.reportId() == null) {
            throw new IllegalArgumentException("Only saved reports can be analyzed.");
        }
        return new AnalysisKey(report.reportId(), report.description());
    }

    private record AnalysisKey(int reportId, String description) {
    }
}
