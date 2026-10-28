package com.example.ai;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GroqReportAnalysisProviderTest {

    @Test
    void reportsMissingApiKeyWithoutMakingARequest() {
        GroqReportAnalysisProvider provider =
                new GroqReportAnalysisProvider("", "openai/gpt-oss-20b");

        ReportAnalysisException exception = assertThrows(ReportAnalysisException.class,
                () -> provider.analyze("A streetlight is out."));

        assertTrue(exception.getMessage().contains("GROQ_API_KEY"));
    }
}
