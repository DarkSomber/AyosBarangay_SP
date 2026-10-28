package com.example.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

class ReportAnalysisParserTest {

    private final ReportAnalysisParser parser = new ReportAnalysisParser(new ObjectMapper());

    @Test
    void parsesCompleteStructuredResponse() {
        ReportAnalysis result = parser.parse("""
                {
                  "urgency": "HIGH",
                  "category": "WATER_AND_DRAINAGE",
                  "summary": "Water pools on the road after rainfall.",
                  "recommendedAction": "Inspect drainage near the reported location.",
                  "suspicious": false,
                  "urgencyReason": "Pooling water may affect road safety.",
                  "suspiciousReason": "No specific indicators were provided."
                }
                """);

        assertEquals(ReportUrgency.HIGH, result.urgency());
        assertEquals(ReportCategory.WATER_AND_DRAINAGE, result.category());
        assertFalse(result.suspicious());
        assertEquals("Inspect drainage near the reported location.", result.recommendedAction());
    }

    @Test
    void rejectsMalformedJson() {
        ReportAnalysisException exception = assertThrows(ReportAnalysisException.class,
                () -> parser.parse("{not-json"));

        assertTrue(exception.getMessage().contains("malformed"));
    }

    @Test
    void rejectsUnsupportedEnums() {
        ReportAnalysisException exception = assertThrows(ReportAnalysisException.class,
                () -> parser.parse(validResponse().replace(
                        "\"urgency\": \"LOW\"", "\"urgency\": \"IMMEDIATE\"")));

        assertTrue(exception.getMessage().contains("unsupported"));
    }

    @Test
    void rejectsMissingAndUnexpectedFields() {
        assertThrows(ReportAnalysisException.class, () -> parser.parse("""
                {"urgency":"LOW"}
                """));
        assertThrows(ReportAnalysisException.class,
                () -> parser.parse(validResponse().replace(
                        "\"suspiciousReason\": \"No specific indicators were provided.\"",
                        "\"suspiciousReason\": \"No specific indicators were provided.\", "
                                + "\"unexpected\": \"value\"")));
    }

    @Test
    void rejectsInvalidFieldTypesAndOverlongText() {
        assertThrows(ReportAnalysisException.class, () -> parser.parse("""
                {
                  "urgency":"LOW","category":"OTHER","summary":"s",
                  "recommendedAction":"a","suspicious":"false",
                  "urgencyReason":"u","suspiciousReason":"r"
                }
                """));
        assertThrows(ReportAnalysisException.class, () -> parser.parse(validResponse().replace(
                "\"summary\": \"A short summary.\"",
                "\"summary\": \"" + "x".repeat(501) + "\"")));
    }

    private String validResponse() {
        return """
                {
                  "urgency": "LOW",
                  "category": "OTHER",
                  "summary": "A short summary.",
                  "recommendedAction": "Review the report.",
                  "suspicious": false,
                  "urgencyReason": "No immediate danger is stated.",
                  "suspiciousReason": "No specific indicators were provided."
                }
                """;
    }
}
