package org.mule.pdf.extension.internal.operation;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.mule.pdf.extension.api.PdfAttributes;
import org.mule.pdf.extension.internal.connection.PdfInternalConnection;
import org.mule.pdf.extension.internal.metadata.BinaryMetadataResolver;
import org.mule.pdf.extension.internal.operation.paging.PdfPagingProvider;
import org.mule.pdf.extension.internal.utils.TimeUtils;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.api.util.Preconditions;
import org.mule.runtime.extension.api.annotation.metadata.TypeResolver;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SplitDocumentOperation {
    private static final Logger logger = LoggerFactory.getLogger(SplitDocumentOperation.class);

    @SuppressWarnings({ "rawtypes" })
    @MediaType(value = MediaType.ANY, strict = false)
    @DisplayName("Split Document")
    public PagingProvider<PdfInternalConnection, Result<CursorProvider, PdfAttributes>> splitDocument(
            //@Config PDFModuleConfiguration config,
            @DisplayName("Original PDF Payload")
                @Content @TypeResolver(BinaryMetadataResolver.class)
                InputStream pdfPayload,
            @DisplayName("Original File Name")
                @Summary("The file name of the PDF Document we will split.")
                String fileName,
            @DisplayName("Split Label Suffix")
                @Example("Part")
                String splitLabel,
            @DisplayName("File Name and Suffix Separator")
                @Example("_")
                String labelSeparator,
            @DisplayName("Start Parts with First Pages")
                @Summary("The original document start pages to include in each part after the split.")
                @Optional
                @Example("4")
                int firstPages,
            @DisplayName("Max Number of Pages in Each Part")
                int maxPages,
            StreamingHelper streamingHelper) throws Exception {
        
        Preconditions.checkArgument(maxPages > firstPages, "Starting page count must be smaller than max pages per PDF part.");
        final long operationStartTime = System.nanoTime();
        final PDDocument originalPdfDocument;
        final int originalPdfTotalPages;
        final Iterator<PDDocument> pdfPartsIterator;
        final int outputPdfParts;
        Iterable<PDDocument> originalPdfDocumentParts = new LinkedList<PDDocument>();
        byte[] startingPagesBytes = null;
        String pdfPartFileNamePrefix = FilenameUtils.removeExtension(fileName) + labelSeparator + splitLabel;
        
        Splitter splitter = new Splitter();

        try {
            originalPdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(pdfPayload));
            originalPdfTotalPages = originalPdfDocument.getNumberOfPages();

            if (firstPages > 0 && firstPages < originalPdfTotalPages) {
                splitter.setStartPage(firstPages + 1);
                splitter.setSplitAtPage(maxPages - firstPages);
                PDDocument pdfPartStartingDocument = new PDDocument();

                for (int i = 0; i < firstPages; i++) {
                    pdfPartStartingDocument.addPage(originalPdfDocument.getPage(i));
                }

                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    pdfPartStartingDocument.save(baos);
                    startingPagesBytes = baos.toByteArray(); // Store bytes for later use
                    pdfPartStartingDocument.close();
                    logger.debug("Starting {} pages for {} pre-serialized to {} bytes.",
                                    firstPages, fileName, startingPagesBytes.length);
                }
                logger.info("Created starting document in {}ms", TimeUtils.getElapsedMillis(operationStartTime));
            } else {
                logger.info("Will split {} every {} pages.", fileName, maxPages);
                splitter.setSplitAtPage(maxPages);
            }
            originalPdfDocumentParts = splitter.split(originalPdfDocument);
            outputPdfParts = ((Collection<?>) originalPdfDocumentParts).size();
            pdfPartsIterator = originalPdfDocumentParts.iterator();

        } finally {
            logger.info("Splitting original PDf document took {}ms", TimeUtils.getElapsedMillis(operationStartTime));
        }
        
        if (outputPdfParts > 1) {
            return new PdfPagingProvider(originalPdfDocument, pdfPartsIterator, outputPdfParts, operationStartTime,
                    startingPagesBytes, fileName, pdfPartFileNamePrefix, streamingHelper);
        } else {
            return new PagingProvider<PdfInternalConnection, Result<CursorProvider, PdfAttributes>>() {

                @Override
                public void close(PdfInternalConnection arg0) throws MuleException {
                    logger.info("Returning {} without splitting in {}ms", fileName,
                                TimeUtils.getElapsedMillis(operationStartTime));
                }

                @Override
                public List<Result<CursorProvider, PdfAttributes>> getPage(PdfInternalConnection arg0) {
                    List<Result<CursorProvider, PdfAttributes>> singleResult = new ArrayList<>();
                    singleResult.add(Result.<CursorProvider, PdfAttributes>builder()
                                .output((CursorProvider) streamingHelper.resolveCursorProvider(pdfPayload))
                                .mediaType(org.mule.runtime.api.metadata.MediaType.parse("application/pdf"))
                                .attributes(null)
                                .build());
                    
                    return singleResult;
                }

                @Override
                public java.util.Optional<Integer> getTotalResults(PdfInternalConnection arg0) {
                    return java.util.Optional.of(1);
                }
            };
        }
    }
}
