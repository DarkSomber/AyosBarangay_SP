package com.example.spam;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ReportSpamCheckServiceTest {

    private final ReportSpamCheckService service = new ReportSpamCheckService();

    @Test
    void flagsCommonJunkPlaceholders() {
        SpamAssessment assessment = service.assess("ASDFGH!!!");

        assertTrue(assessment.flagged());
        assertTrue(assessment.reasons().getFirst().contains("placeholder"));
    }

    @Test
    void flagsDescriptionsWithoutReadableText() {
        SpamAssessment assessment = service.assess("!!! 😀 ???");

        assertTrue(assessment.flagged());
        assertTrue(assessment.reasons().getFirst().contains("readable"));
    }

    @Test
    void flagsAnUnusuallyLongRepeatedCharacterRun() {
        SpamAssessment assessment = service.assess("aaaaaaaaaaaa lamp is broken");

        assertTrue(assessment.flagged());
        assertTrue(assessment.reasons().getFirst().contains("repeated-character"));
    }

    @Test
    void doesNotFlagLegitimateShortReport() {
        assertFalse(service.assess("May sira na ilaw").flagged());
    }

    @Test
    void doesNotFlagFilipinoLanguageReports() {
        assertFalse(service.assess("May baha sa kalsada pagkatapos ng malakas na ulan.").flagged());
    }

    @Test
    void doesNotFlagOrdinaryRepeatedLettersOrReportTerms() {
        assertFalse(service.assess("May butas sa kalsada malapit sa barangay hall.").flagged());
    }
}
