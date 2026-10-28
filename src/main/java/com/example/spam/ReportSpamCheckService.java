package com.example.spam;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

/**
 * Local, advisory-only checks for unmistakable low-quality report text:
 * no readable letters or numbers, a known placeholder/keyboard mash, or a
 * run of at least eight repeated characters. Short descriptions are not
 * flagged solely for being short, and repeated reports by the same resident
 * are intentionally not treated as spam.
 */
@Service
public class ReportSpamCheckService {

    private static final Pattern REPEATED_CHARACTER = Pattern.compile("(.)\\1{7,}");
    private static final Set<String> JUNK_PLACEHOLDERS = Set.of(
            "asdf", "asdfgh", "qwerty", "qwertyuiop", "zxcv", "zxcvbn",
            "lorem ipsum", "blah blah", "test test", "spam spam");

    public SpamAssessment assess(String description) {
        String normalized = normalize(description);
        List<String> reasons = new ArrayList<>();

        if (normalized.isEmpty()) {
            reasons.add("Description contains no readable letters or numbers.");
            return new SpamAssessment(reasons);
        }
        if (JUNK_PLACEHOLDERS.contains(normalized)) {
            reasons.add("Description matches a common placeholder or keyboard-mash pattern.");
        }
        if (REPEATED_CHARACTER.matcher(description).find()) {
            reasons.add("Description contains an unusually long repeated-character sequence.");
        }

        return new SpamAssessment(reasons);
    }

    private String normalize(String description) {
        return description.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }
}
