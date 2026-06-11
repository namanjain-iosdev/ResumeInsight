package com.cvanalyzer.service.pdf;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extracts a {@link PdfStyleProfile} from an original resume PDF using PDFBox.
 * Builds a histogram of glyph font sizes to infer body vs heading sizes,
 * detects bold/serif heading fonts, and measures text-box margins.
 */
@Component
@Slf4j
public class PdfStyleAnalyzer {

    public PdfStyleProfile analyze(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            return PdfStyleProfile.defaults();
        }
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            Collector collector = new Collector();
            collector.setStartPage(1);
            collector.setEndPage(1); // first page is representative of the style
            collector.getText(document);

            PDPage firstPage = document.getPage(0);
            PDRectangle box = firstPage.getMediaBox();

            return collector.toProfile(box);
        } catch (IOException | RuntimeException e) {
            log.warn("PDF style analysis failed, falling back to defaults: {}", e.getMessage());
            return PdfStyleProfile.defaults();
        }
    }

    /** Custom stripper that records per-glyph size/position instead of emitting text. */
    private static class Collector extends PDFTextStripper {
        private final Map<Integer, Integer> sizeHistogram = new HashMap<>();
        private float maxSize = 0;
        private String maxSizeFont = "";
        private float minX = Float.MAX_VALUE, maxX = 0, minY = Float.MAX_VALUE, maxY = 0;
        private float dominantFontTotalHeight = 0;
        private int dominantGlyphCount = 0;
        private String anyFontName = "";

        Collector() throws IOException {
            super();
        }

        @Override
        protected void writeString(String text, List<TextPosition> positions) {
            for (TextPosition p : positions) {
                float size = p.getFontSizeInPt();
                int rounded = Math.round(size);
                if (rounded <= 0) continue;
                sizeHistogram.merge(rounded, 1, Integer::sum);

                if (size > maxSize) {
                    maxSize = size;
                    maxSizeFont = fontName(p);
                }
                if (anyFontName.isEmpty()) anyFontName = fontName(p);

                float x = p.getXDirAdj();
                float yTop = p.getYDirAdj();
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x + p.getWidthDirAdj());
                minY = Math.min(minY, yTop - p.getHeightDir());
                maxY = Math.max(maxY, yTop);
            }
        }

        private String fontName(TextPosition p) {
            try {
                return p.getFont() != null && p.getFont().getName() != null ? p.getFont().getName() : "";
            } catch (Exception e) {
                return "";
            }
        }

        PdfStyleProfile toProfile(PDRectangle box) {
            if (sizeHistogram.isEmpty()) {
                return PdfStyleProfile.defaults();
            }
            // Body font size = most frequent glyph size.
            int bodySize = sizeHistogram.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(11);

            // Heading size = a clearly larger size (max), bounded so it stays sane.
            float headingSize = Math.max(bodySize + 1.5f, Math.min(maxSize, bodySize * 1.6f));

            String headingFont = maxSizeFont.toLowerCase();
            boolean bold = headingFont.contains("bold") || headingFont.contains("black") || headingFont.contains("semibold");
            boolean serif = anyFontName.toLowerCase().matches(".*(times|serif|georgia|garamond|minion).*");

            float pageWidth = box.getWidth();
            float pageHeight = box.getHeight();

            // PDFBox y origin differs; clamp margins to non-negative reasonable values.
            float marginLeft = clamp(minX == Float.MAX_VALUE ? 40 : minX, 18, pageWidth / 3);
            float marginRight = clamp(maxX == 0 ? 40 : (pageWidth - maxX), 18, pageWidth / 3);
            float marginTop = clamp(minY == Float.MAX_VALUE ? 40 : (pageHeight - maxY), 18, pageHeight / 4);
            float marginBottom = clamp(minY == Float.MAX_VALUE ? 40 : minY, 18, pageHeight / 4);

            return PdfStyleProfile.builder()
                    .bodyFontSize(bodySize)
                    .headingFontSize(headingSize)
                    .headingBold(bold)
                    .serif(serif)
                    .marginLeft(marginLeft)
                    .marginRight(marginRight)
                    .marginTop(marginTop)
                    .marginBottom(marginBottom)
                    .leading(bodySize * 1.25f)
                    .pageWidth(pageWidth)
                    .pageHeight(pageHeight)
                    .build();
        }

        private float clamp(float v, float lo, float hi) {
            return Math.max(lo, Math.min(hi, v));
        }
    }
}
