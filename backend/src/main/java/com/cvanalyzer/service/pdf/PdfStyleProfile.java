package com.cvanalyzer.service.pdf;

import lombok.Builder;
import lombok.Data;

/**
 * A compact description of the original resume's visual structure, extracted
 * from the source PDF. Used to regenerate the tailored resume with the same
 * look-and-feel rather than a generic template (Feature 4).
 *
 * <p>This is a best-effort profile: true pixel-perfect reproduction is not
 * possible once text is reflowed, but font sizes, weights, margins, spacing
 * and bullet style are preserved.
 */
@Data
@Builder
public class PdfStyleProfile {
    private float bodyFontSize;
    private float headingFontSize;
    private boolean headingBold;

    /** Whether the dominant font appears to be serif (Times-like) vs sans. */
    private boolean serif;

    private float marginLeft;
    private float marginRight;
    private float marginTop;
    private float marginBottom;

    /** Leading (line spacing) inferred from the original body text. */
    private float leading;

    private float pageWidth;
    private float pageHeight;

    /** A sensible default profile when analysis fails or the source isn't a PDF. */
    public static PdfStyleProfile defaults() {
        return PdfStyleProfile.builder()
                .bodyFontSize(10.5f)
                .headingFontSize(13.5f)
                .headingBold(true)
                .serif(false)
                .marginLeft(40f)
                .marginRight(40f)
                .marginTop(40f)
                .marginBottom(40f)
                .leading(13f)
                .pageWidth(595f)   // A4 portrait points
                .pageHeight(842f)
                .build();
    }
}
