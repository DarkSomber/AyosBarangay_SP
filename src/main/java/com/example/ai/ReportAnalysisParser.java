package com.example.ai;

import java.util.Set;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public final class ReportAnalysisParser {

    private static final Set<String> FIELDS = Set.of(
            "urgency", "category", "summary", "recommendedAction",
            "suspicious", "urgencyReason", "suspiciousReason");

    private final ObjectMapper objectMapper;

    public ReportAnalysisParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ReportAnalysis parse(String json) {
        final JsonNode root;
        try {
            root = objectMapper.readTree(json);
        } catch (JacksonException exception) {
            throw new ReportAnalysisException("Groq returned malformed analysis JSON.", exception);
        }
        if (root == null || !root.isObject() || !hasExactFields(root)) {
            throw new ReportAnalysisException("Groq returned incomplete or unexpected analysis fields.");
        }

        JsonNode urgency = root.get("urgency");
        JsonNode category = root.get("category");
        JsonNode summary = root.get("summary");
        JsonNode action = root.get("recommendedAction");
        JsonNode suspicious = root.get("suspicious");
        JsonNode urgencyReason = root.get("urgencyReason");
        JsonNode suspiciousReason = root.get("suspiciousReason");
        if (!urgency.isTextual() || !category.isTextual()
                || !summary.isTextual() || !action.isTextual()
                || !suspicious.isBoolean() || !urgencyReason.isTextual()
                || !suspiciousReason.isTextual()) {
            throw new ReportAnalysisException("Groq returned analysis fields with invalid types.");
        }

        final ReportUrgency parsedUrgency;
        final ReportCategory parsedCategory;
        try {
            parsedUrgency = ReportUrgency.valueOf(urgency.textValue());
            parsedCategory = ReportCategory.valueOf(category.textValue());
        } catch (IllegalArgumentException exception) {
            throw new ReportAnalysisException("Groq returned an unsupported urgency or category.", exception);
        }
        try {
            return new ReportAnalysis(
                    parsedUrgency,
                    parsedCategory,
                    summary.textValue(),
                    action.textValue(),
                    suspicious.booleanValue(),
                    urgencyReason.textValue(),
                    suspiciousReason.textValue());
        } catch (IllegalArgumentException exception) {
            throw new ReportAnalysisException(
                    "Groq returned analysis text that is empty or exceeds the allowed length.", exception);
        }
    }

    private boolean hasExactFields(JsonNode root) {
        if (root.size() != FIELDS.size()) {
            return false;
        }
        for (String field : FIELDS) {
            if (!root.has(field)) {
                return false;
            }
        }
        return true;
    }
}
