package com.example.ai;

@FunctionalInterface
public interface ReportAnalysisProvider {
    ReportAnalysis analyze(String reportDescription);

    default String summarize(String reportsText) {
        throw new ReportAnalysisException("This analysis provider cannot write summaries.");
    }
}
