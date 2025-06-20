package org.mule.pdf.extension.internal.operation.paging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.mule.pdf.extension.api.PdfAttributes;
import org.mule.pdf.extension.internal.connection.PdfInternalConnection;
import org.mule.pdf.extension.internal.utils.TimeUtils;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public class PdfPagingProvider
        implements PagingProvider<PdfInternalConnection, Result<CursorProvider, PdfAttributes>> {
    private static final Logger logger = LoggerFactory.getLogger(PdfPagingProvider.class);
    private static final Integer PDF_PART_PAGE_SIZE = 5;

    private final String pdfPartFileNameSuffix;
    private final StreamingHelper streamingHelper;
    private final AtomicBoolean isOriginalPdfDocumentLoaded = new AtomicBoolean(false);

    private final long operationStartTime;
    private final byte[] startingPagesBytes;
    private final PDDocument originalPdfDocument;
    private final String originalPdfFileName;
    private final Iterator<PDDocument> pdfPartsIterator;
    private final int totalPdfParts;
    private int pdfPartCounter = 1;

    public PdfPagingProvider(
                    PDDocument originalPdfDocument,
                    Iterator<PDDocument> pdfPartsIterator,
                    int outputPdfParts,
                    long operationStartTime,
                    byte[] startingPagesBytes,
                    String originalPdfFileName,
                    String pdfPartFileNameSuffix,
                    StreamingHelper streamingHelper) {
        this.originalPdfDocument = originalPdfDocument;
        this.operationStartTime = operationStartTime;
        this.pdfPartsIterator = pdfPartsIterator;
        this.totalPdfParts = outputPdfParts;
        this.startingPagesBytes = startingPagesBytes;
        this.originalPdfFileName = originalPdfFileName;
        this.pdfPartFileNameSuffix = pdfPartFileNameSuffix;
        this.streamingHelper = streamingHelper;
    }

    Splitter splitter = new Splitter();

    @Override
    public List<Result<CursorProvider, PdfAttributes>> getPage(PdfInternalConnection dummyConnection) {
        if (isOriginalPdfDocumentLoaded.compareAndSet(false, true)) {
            logger.info("Initiating Paging Operation for {}", this.originalPdfFileName);
        }

        if (pdfPartsIterator.hasNext()) {
            long startPagination = System.nanoTime();
            List<Result<CursorProvider, PdfAttributes>> page = new LinkedList<>();
            for (int i = 0; i < PDF_PART_PAGE_SIZE ; i++) {
                PDDocument pdfDocumentPart = pdfPartsIterator.next();
                try {
                    // Collect metadata
                    PdfAttributes attributes = new PdfAttributes();
                    attributes.setPageCount(pdfDocumentPart.getNumberOfPages());
                    // attributes.setFileSize(outputStream.size());
                    attributes.setFileName(this.pdfPartFileNameSuffix + String.format("%03d", this.pdfPartCounter) + ".pdf");

                    pdfPartCounter++;

                    page.add(Result.<CursorProvider, PdfAttributes>builder()
                        .output((CursorProvider) streamingHelper.resolveCursorProvider(getPdfPartAsStream(pdfDocumentPart)))
                        .mediaType(org.mule.runtime.api.metadata.MediaType.parse("application/pdf"))
                        .attributes(attributes)
                        .build());

                } finally {
                    try {
                        pdfDocumentPart.close();
                    } catch (IOException e) {
                        logger.error("Exception closing the current PDF part resource.");
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            logger.debug("Page of {} PDF parts sent in {}ms.", page.size(), 
                                    TimeUtils.getElapsedMillis(startPagination));

            return page;
        } else {
            return Collections.emptyList();
        }

    }

    private InputStream getPdfPartAsStream(PDDocument pdfDocumentPart) {
        final long pdfAsStreamStartTime = System.nanoTime();

        try ( ByteArrayOutputStream outputStream = new ByteArrayOutputStream() ) {
            if (this.startingPagesBytes != null && this.startingPagesBytes.length > 0) {
                PDFMergerUtility pdfMerger = new PDFMergerUtility();
                pdfMerger.setDocumentMergeMode(PDFMergerUtility.DocumentMergeMode.OPTIMIZE_RESOURCES_MODE);

                PDDocument startingPagesAndCurrentPdfParDocument = Loader.loadPDF(this.startingPagesBytes);
                pdfMerger.appendDocument(startingPagesAndCurrentPdfParDocument, pdfDocumentPart);
                startingPagesAndCurrentPdfParDocument.save(outputStream);

                startingPagesAndCurrentPdfParDocument.close();

                logger.debug("Merged starting pages with current PDF Part in {}ms", 
                                    TimeUtils.getElapsedMillis(pdfAsStreamStartTime));
            } else {
                pdfDocumentPart.save(outputStream);
            }

            logger.debug("PDF Part InputStream created in {}ms", TimeUtils.getElapsedMillis(pdfAsStreamStartTime));

            return (InputStream) new ByteArrayInputStream(outputStream.toByteArray());

        } catch (IOException e) {
            logger.error("Exception: " + e.getMessage());
            e.printStackTrace();
            // TODO: fix this condition
            return null;
        }
    }

    @Override
    public Optional<Integer> getTotalResults(PdfInternalConnection dummyConnection) {
        logger.info("Total {} PDF parts to be processed for {}", this.totalPdfParts, this.originalPdfFileName);
        return java.util.Optional.empty();  //of(this.totalPdfParts);
    }

    @Override
    public void close(PdfInternalConnection dummyConnection) throws MuleException {
        logger.info("Finished Split Document operation for {} in {}ms", this.originalPdfFileName, TimeUtils.getElapsedMillis(operationStartTime));
        try {
            originalPdfDocument.close();
        } catch (IOException e) {
            logger.error("Failure cleaning original pdf file resources");
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
