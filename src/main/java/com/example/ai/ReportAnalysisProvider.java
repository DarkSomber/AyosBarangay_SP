package com.example.ai;

@FunctionalInterface
public interface ReportAnalysisProvider {
    ReportAnalysis analyze(String reportDescription);
}
