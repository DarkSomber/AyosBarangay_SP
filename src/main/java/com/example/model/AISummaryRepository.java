package com.example.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** MySQL access for saved AI summaries (table ai_summaries). */
@Repository
public class AISummaryRepository {

    private final JdbcClient jdbc;

    public AISummaryRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public AISummary save(AISummary s) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.sql("""
                INSERT INTO ai_summaries (generated_by, summary_content, report_count,
                                          summary_period_start, summary_period_end, generated_at)
                VALUES (:by, :content, :count, :start, :end, :generatedAt)
                """)
                .param("by", s.generatedBy())
                .param("content", s.summaryContent())
                .param("count", s.reportCount())
                .param("start", s.summaryPeriodStart())
                .param("end", s.summaryPeriodEnd())
                .param("generatedAt", s.generatedAt())
                .update(keys);
        return new AISummary(keys.getKey().intValue(), s.generatedBy(), s.summaryContent(), s.reportCount(),
                s.summaryPeriodStart(), s.summaryPeriodEnd(), s.generatedAt());
    }

    public List<AISummary> findRecent(int limit) {
        return jdbc.sql("SELECT * FROM ai_summaries ORDER BY generated_at DESC, summary_id DESC LIMIT :limit")
                .param("limit", limit)
                .query(this::map)
                .list();
    }

    private AISummary map(ResultSet rs, int rowNum) throws SQLException {
        return new AISummary(
                rs.getInt("summary_id"),
                rs.getInt("generated_by"),
                rs.getString("summary_content"),
                rs.getInt("report_count"),
                rs.getObject("summary_period_start", LocalDate.class),
                rs.getObject("summary_period_end", LocalDate.class),
                rs.getObject("generated_at", LocalDateTime.class));
    }
}
