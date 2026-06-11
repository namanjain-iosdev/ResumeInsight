package com.cvanalyzer.service.pdf;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Renders optimized resume text into a PDF that mirrors the original's visual
 * structure (Feature 4): same page size, margins, body/heading font sizes,
 * heading weight, serif/sans family, bullet formatting and spacing.
 *
 * <p>It maps each line of the optimized content onto a role (heading / bullet /
 * body) and styles it according to the supplied {@link PdfStyleProfile}.
 */
@Component
@Slf4j
public class FormattedPdfGenerator {

    private static final Pattern BULLET = Pattern.compile("^\\s*[\\-•*▪◦·]\\s+(.*)$");
    private static final Pattern KNOWN_HEADING = Pattern.compile(
            "(?i)^(professional\\s+)?(summary|objective|profile|experience|work\\s+experience|" +
            "employment|education|skills|technical\\s+skills|projects|certifications?|" +
            "achievements?|awards?|contact|languages?|interests|publications|references|volunteer)\\s*:?$");

    public byte[] generate(String content, PdfStyleProfile profile) {
        PdfStyleProfile p = profile != null ? profile : PdfStyleProfile.defaults();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, new PageSize(p.getPageWidth(), p.getPageHeight()));
            document.setMargins(p.getMarginTop(), p.getMarginRight(), p.getMarginBottom(), p.getMarginLeft());

            PdfFont bodyFont = PdfFontFactory.createFont(
                    p.isSerif() ? StandardFonts.TIMES_ROMAN : StandardFonts.HELVETICA);
            PdfFont headingFont = PdfFontFactory.createFont(
                    p.isSerif()
                            ? (p.isHeadingBold() ? StandardFonts.TIMES_BOLD : StandardFonts.TIMES_ROMAN)
                            : (p.isHeadingBold() ? StandardFonts.HELVETICA_BOLD : StandardFonts.HELVETICA));

            String[] lines = content == null ? new String[0] : content.split("\n", -1);
            boolean firstNonEmptySeen = false;

            for (int i = 0; i < lines.length; i++) {
                String raw = lines[i];
                String line = raw.strip();

                if (line.isEmpty()) {
                    // Preserve vertical spacing with a small empty paragraph.
                    Paragraph spacer = new Paragraph(" ")
                            .setFontSize(p.getBodyFontSize() * 0.5f)
                            .setMarginTop(0).setMarginBottom(0);
                    document.add(spacer);
                    continue;
                }

                java.util.regex.Matcher bulletMatcher = BULLET.matcher(raw);
                if (bulletMatcher.matches()) {
                    Paragraph bullet = new Paragraph("•  " + bulletMatcher.group(1).strip())
                            .setFont(bodyFont)
                            .setFontSize(p.getBodyFontSize())
                            .setMarginLeft(14f)
                            .setMarginTop(1f).setMarginBottom(1f)
                            .setMultipliedLeading(p.getLeading() / Math.max(1f, p.getBodyFontSize()));
                    document.add(bullet);
                    continue;
                }

                boolean heading = isHeading(line);
                // The very first non-empty line is usually the candidate's name → emphasize.
                boolean nameLine = !firstNonEmptySeen;
                firstNonEmptySeen = true;

                if (nameLine) {
                    Paragraph name = new Paragraph(line)
                            .setFont(headingFont)
                            .setFontSize(p.getHeadingFontSize() * 1.25f)
                            .setBold()
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginBottom(2f);
                    document.add(name);
                } else if (heading) {
                    Paragraph h = new Paragraph(line.toUpperCase())
                            .setFont(headingFont)
                            .setFontSize(p.getHeadingFontSize())
                            .setMarginTop(8f).setMarginBottom(2f);
                    if (p.isHeadingBold()) h.setBold();
                    document.add(h);
                } else {
                    Paragraph body = new Paragraph(line)
                            .setFont(bodyFont)
                            .setFontSize(p.getBodyFontSize())
                            .setMarginTop(0.5f).setMarginBottom(0.5f)
                            .setMultipliedLeading(p.getLeading() / Math.max(1f, p.getBodyFontSize()));
                    document.add(body);
                }
            }

            document.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate formatted PDF", e);
        }
    }

    private boolean isHeading(String line) {
        if (line.length() > 45) return false;
        if (KNOWN_HEADING.matcher(line).matches()) return true;
        // Short, all-caps, no sentence punctuation → treat as a section heading.
        boolean allCaps = line.equals(line.toUpperCase()) && line.matches(".*[A-Z].*");
        boolean noEndPunctuation = !line.matches(".*[.,;].*");
        return allCaps && noEndPunctuation && line.split("\\s+").length <= 5;
    }
}
