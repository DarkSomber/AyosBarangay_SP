package com.example.ai;

public class ReportAnalysisException extends RuntimeException {

    public ReportAnalysisException(String message) {
        super(message);
    }

    public ReportAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
