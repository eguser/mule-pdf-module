package org.mule.pdf.extension.internal.operation.paging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.mule.pdf.extension.api.PdfAttributes;
import org.mule.pdf.extension.internal.connection.PdfInternalConnection;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SplitPdfPagingProvider implements PagingProvider<PdfInternalConnection, Result<CursorProvider, PdfAttributes>> {
    private static final Logger logger = LoggerFactory.getLogger(SplitPdfPagingProvider.class);
    private final InputStream originalPdfPayload;
    private final String pdfPartFileName;
    private final int pdfPartMaxPages;
    private final int pdfPartStartingPages;
    private final StreamingHelper streamingHelper;
    private final AtomicBoolean isOriginalPdfDocumentLoaded = new AtomicBoolean(false);

    private PDDocument originalPdfDocument;
    private int originalPdfTotalPages;
    private PDDocument pdfPartStartingDocument;
    private Iterator<PDDocument> pdfPartsIterator;
    private Iterable<PDDocument> originalPdfDocumentParts = new ArrayList<>();
    private int totalPdfParts;
    private int pdfPartCounter = 1;

    public SplitPdfPagingProvider(InputStream pdfPayload, String fileName, String splitLabel, String labelSeparator,  int partMaxPages, int partStartingPages, StreamingHelper streamingHelper) {
        this.originalPdfPayload = pdfPayload;
        this.pdfPartFileName = FilenameUtils.removeExtension(fileName) + labelSeparator + splitLabel;
        this.pdfPartMaxPages = partMaxPages;
        this.pdfPartStartingPages = partStartingPages;
        this.streamingHelper = streamingHelper;
    }

    Splitter splitter = new Splitter();
    PDFMergerUtility pdfMerger = new PDFMergerUtility();

    @Override
    public List<Result<CursorProvider, PdfAttributes>> getPage(PdfInternalConnection dummyConnection) {
        if (isOriginalPdfDocumentLoaded.compareAndSet(false, true)) {
            logger.info("Initiating Split Document...");
            splitPdfDocument();
        }
        
        if (pdfPartsIterator.hasNext()) {
            logger.info("Working on next response Page");
            List<Result<CursorProvider, PdfAttributes>> page = new ArrayList<>();
            for (int i = 0; i < 5 && pdfPartsIterator.hasNext(); i++) {

                PDDocument pdfDocumentPart = pdfPartsIterator.next();
                // Collect metadata
                PdfAttributes attributes = new PdfAttributes();
                attributes.setPageCount(pdfDocumentPart.getNumberOfPages());
                // attributes.setFileSize(outputStream.size());
                attributes.setFileName(this.pdfPartFileName + String.format("%03d", this.pdfPartCounter) + ".pdf");

                pdfPartCounter++;

                page.add(Result.<CursorProvider, PdfAttributes>builder()
                    .output((CursorProvider) streamingHelper.resolveCursorProvider(getPdfPartAsStream(pdfDocumentPart)))
                    .mediaType(org.mule.runtime.api.metadata.MediaType.parse("application/pdf"))
                    .attributes(attributes)
                    .build());

            }
            return page;
        } else {
            return Collections.emptyList();
        }

    }

    private void splitPdfDocument() {
        try {

            this.originalPdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(originalPdfPayload));
            this.originalPdfTotalPages = originalPdfDocument.getNumberOfPages();

            if (this.pdfPartStartingPages > 0 && this.pdfPartStartingPages < this.originalPdfTotalPages) {
                splitter.setStartPage(pdfPartStartingPages + 1);
                splitter.setSplitAtPage(pdfPartMaxPages - pdfPartStartingPages);
                this.pdfPartStartingDocument = new PDDocument();

                for (int i = 0; i < pdfPartStartingPages; i++) {
                    pdfPartStartingDocument.addPage(originalPdfDocument.getPage(i));
                }
                pdfMerger.setDocumentMergeMode(PDFMergerUtility.DocumentMergeMode.OPTIMIZE_RESOURCES_MODE);
                logger.info("Starting document has {} pages", pdfPartStartingDocument.getNumberOfPages());
            } else {
                logger.info("Splitting every {} ", pdfPartMaxPages);
                splitter.setSplitAtPage(pdfPartMaxPages);
            }
            originalPdfDocumentParts = splitter.split(originalPdfDocument);
            this.totalPdfParts = ((Collection<?>) originalPdfDocumentParts).size();
            pdfPartsIterator = originalPdfDocumentParts.iterator();

        } catch (IOException e) {
            logger.error("Error splitting PDF file:");
            e.printStackTrace();
        }

    }
    
    private InputStream getPdfPartAsStream(PDDocument pdfDocumentPart) {

        try (
            PDDocument mergeStartingAndPart = new PDDocument();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ) {
            if (pdfPartStartingDocument != null) {
                pdfMerger.appendDocument(mergeStartingAndPart, pdfPartStartingDocument);
                pdfMerger.appendDocument(mergeStartingAndPart, pdfDocumentPart);
                mergeStartingAndPart.save(outputStream);
            } else {
                pdfDocumentPart.save(outputStream);
            }
            
            InputStream pdfPartStream = new ByteArrayInputStream(outputStream.toByteArray());

            pdfDocumentPart.close();
            return pdfPartStream;

        } catch (IOException e) {
            logger.error("Exception: " + e.getMessage());
            e.printStackTrace();
            // TODO: fix this condition
            return null;
        }
    }

    @Override
    public Optional<Integer> getTotalResults(PdfInternalConnection dummyConnection) {
        return java.util.Optional.of(this.totalPdfParts);
    }

    @Override
    public void close(PdfInternalConnection dummyConnection) throws MuleException {
        logger.info("Closing resources...");
        try {
            pdfPartStartingDocument.close();
            originalPdfDocument.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
