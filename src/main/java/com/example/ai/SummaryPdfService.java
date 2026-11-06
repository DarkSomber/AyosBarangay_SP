package com.example.ai;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.example.config.BarangayInfo;
import com.example.model.AISummary;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;

@Service
public class SummaryPdfService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a");
    private static final Pattern MD_HEADING = Pattern.compile("^#{1,6}\\s+(.*)$");
    private static final Pattern BOLD_LINE = Pattern.compile("^\\*\\*([^*]+)\\*\\*:?$");
    private static final Pattern NUMBERED = Pattern.compile("^(\\d+)\\.\\s+(.*)$");
    private static final Pattern BULLET = Pattern.compile("^[-*\u2022]\\s+(.*)$");
    private static final Pattern BOLD = Pattern.compile("\\*\\*(.+?)\\*\\*");

    private static final Color GREEN = new Color(0x2A, 0x5A, 0x2C);
    private static final Color GRAY = new Color(0x55, 0x55, 0x55);
    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA, 20, Font.BOLD, GREEN);
    private static final Font SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, GRAY);
    private static final Font HEADING = FontFactory.getFont(FontFactory.HELVETICA, 13, Font.BOLD, GREEN);
    private static final Font BODY = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, Color.BLACK);
    private static final Font BODY_BOLD = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.BOLD, Color.BLACK);
    private static final Font META = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL, GRAY);
    private static final Font META_BOLD = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD, GRAY);

    private final BarangayInfo info;

    public SummaryPdfService(BarangayInfo info) {
        this.info = info;
    }

    public byte[] build(AISummary summary) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 54, 54, 54, 54);
        try {
            PdfWriter.getInstance(doc, out);
            HeaderFooter footer = new HeaderFooter(new Phrase("Page ", META), true);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setBorder(Rectangle.NO_BORDER);
            doc.setFooter(footer);
            doc.open();
            addHeader(doc, summary);
            addBody(doc, summary.summaryContent());
            doc.close();
        } catch (DocumentException ex) {
            throw new IllegalStateException("Could not build the PDF.", ex);
        }
        return out.toByteArray();
    }

    private void addHeader(Document doc, AISummary s) throws DocumentException {
        String barangay = info.name() == null ? "Barangay" : info.name();
        Paragraph title = new Paragraph(sanitize(barangay), TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        Paragraph subtitle = new Paragraph("AI Report Summary", SUBTITLE);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(16);
        doc.add(title);
        doc.add(subtitle);
        doc.add(meta("Period: ", s.summaryPeriodStart().format(DATE) + " to " + s.summaryPeriodEnd().format(DATE)));
        doc.add(meta("Reports analyzed: ", String.valueOf(s.reportCount())));
        doc.add(meta("Generated: ", s.generatedAt().format(STAMP)));
        Paragraph rule = new Paragraph(new Chunk(new LineSeparator(0.8f, 100, GREEN, Element.ALIGN_CENTER, -2)));
        rule.setSpacingAfter(8);
        doc.add(rule);
    }

    private Paragraph meta(String label, String value) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label, META_BOLD));
        p.add(new Chunk(value, META));
        return p;
    }

    private void addBody(Document doc, String content) throws DocumentException {
        for (String raw : sanitize(content).split("\\r?\\n")) {
            String line = raw.strip();
            if (line.isEmpty()) {
                continue;
            }
            Matcher md = MD_HEADING.matcher(line);
            Matcher boldLine = BOLD_LINE.matcher(line);
            Matcher numbered = NUMBERED.matcher(line);
            Matcher bullet = BULLET.matcher(line);
            if (md.matches()) {
                doc.add(heading(md.group(1)));
            } else if (boldLine.matches()) {
                doc.add(heading(boldLine.group(1)));
            } else if (numbered.matches()) {
                doc.add(item(numbered.group(1) + ".", numbered.group(2)));
            } else if (bullet.matches()) {
                doc.add(item("\u2022", bullet.group(1)));
            } else {
                Paragraph p = inline(line);
                p.setSpacingAfter(6);
                doc.add(p);
            }
        }
    }

    private Paragraph heading(String text) {
        Paragraph p = new Paragraph(text.replace("*", "").strip(), HEADING);
        p.setSpacingBefore(12);
        p.setSpacingAfter(4);
        return p;
    }

    private Paragraph item(String marker, String text) {
        Paragraph p = inline(marker + " " + text);
        p.setIndentationLeft(18);
        p.setFirstLineIndent(-14);
        p.setSpacingAfter(4);
        return p;
    }

    private Paragraph inline(String text) {
        Paragraph p = new Paragraph();
        p.setLeading(15);
        Matcher m = BOLD.matcher(text);
        int last = 0;
        while (m.find()) {
            if (m.start() > last) {
                p.add(new Chunk(text.substring(last, m.start()).replace("**", ""), BODY));
            }
            p.add(new Chunk(m.group(1), BODY_BOLD));
            last = m.end();
        }
        if (last < text.length()) {
            p.add(new Chunk(text.substring(last).replace("**", ""), BODY));
        }
        return p;
    }

    private static String sanitize(String text) {
        CharsetEncoder encoder = Charset.forName("windows-1252").newEncoder();
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c == '\u2010' || c == '\u2011' || c == '\u2212') {
                sb.append('-');
            } else if (c == '\u2009' || c == '\u202f') {
                sb.append(' ');
            } else if (c == '\n' || c == '\r' || encoder.canEncode(c)) {
                sb.append(c);
            } else {
                sb.append(' ');
            }
        }
        return sb.toString();
    }
}