package com.example.ai;

@FunctionalInterface
public interface ReportAnalysisProvider {
    ReportAnalysis analyze(String reportDescription);

    /** Plain-text summary of many reports. Providers that cannot do this keep the default. */
    default String summarize(String reportsText) {
        throw new ReportAnalysisException("This analysis provider cannot write summaries.");
    }
}
