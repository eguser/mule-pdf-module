package org.mule.extension.pdf.api;

import java.io.InputStream;

import org.mule.runtime.api.metadata.TypedValue;

public class PdfSplitOperationResult {

    private PdfAttributes pdfAttributes;
    private TypedValue<InputStream> pdfPart;

    public PdfSplitOperationResult() {

    }
    
    public PdfSplitOperationResult(TypedValue<InputStream> pdfPart, PdfAttributes attributes) {
        this.pdfPart = pdfPart;
        this.pdfAttributes = attributes;
    }

    public TypedValue<InputStream> getPdfPart() {
        return pdfPart;
    }

    public PdfAttributes getPdfAttributes() {
        return pdfAttributes;
    }

}
