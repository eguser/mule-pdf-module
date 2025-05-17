package org.mule.pdf.extension.internal.operation;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.mule.pdf.extension.api.PdfAttributes;
import org.mule.pdf.extension.internal.metadata.BinaryMetadataResolver;
import org.mule.runtime.extension.api.annotation.metadata.TypeResolver;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.slf4j.Logger;

public class SplitDocumentOperation {
    private static final Logger LOGGER = getLogger(SplitDocumentOperation.class);

    @MediaType(value = MediaType.ANY, strict = false)
    @DisplayName("Split Document")
    public List<Result<InputStream, PdfAttributes>> splitDocument(
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

        List<Result<InputStream, PdfAttributes>> pdfPartsList = new ArrayList<>();

        String partFileName = FilenameUtils.removeExtension(fileName) + labelSeparator + splitLabel;

        PDDocument startWithDocument = new PDDocument();

        Splitter splitter = new Splitter();
        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDocumentMergeMode(PDFMergerUtility.DocumentMergeMode.OPTIMIZE_RESOURCES_MODE);

        LOGGER.info("Will prepend the first " + firstPages + " pages to each PDF part.");

        try (PDDocument originalPdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(pdfPayload))) {

            int totalPages = originalPdfDocument.getNumberOfPages();

            if (totalPages > maxPages && firstPages > 0 && maxPages > firstPages) {
                splitter.setStartPage(firstPages + 1);
                splitter.setSplitAtPage(maxPages - firstPages);

                for (int i = 0; i < firstPages; i++) {
                    startWithDocument.addPage(originalPdfDocument.getPage(i));
                }

            } else if (totalPages > maxPages) {
                splitter.setSplitAtPage(maxPages);
            }

            int pdfPartsCounter = 0;
            
            if (totalPages > maxPages) {
                List<PDDocument> originalPdfDocumentParts = splitter.split(originalPdfDocument);

                for (PDDocument pdfDocumentPart : originalPdfDocumentParts) {
                    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                        if (startWithDocument.getNumberOfPages() > 0) {
                            PDDocument mergeDocs = new PDDocument();
                            merger.appendDocument(mergeDocs, startWithDocument);
                            merger.appendDocument(mergeDocs, pdfDocumentPart);
                            mergeDocs.save(outputStream);
                            // merger.setDestinationStream(outputStream);
                            // merger.mergeDocuments(null);
                        } else {
                            pdfDocumentPart.save(outputStream);
                        }

                        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

                        // Collect metadata
                        PdfAttributes attributes = new PdfAttributes();
                        attributes.setPageCount(pdfDocumentPart.getNumberOfPages());
                        attributes.setFileSize(outputStream.size());
                        attributes.setFileName(partFileName + String.format("%03d",pdfPartsCounter) + ".pdf");

                        pdfPartsList.add(Result.<InputStream, PdfAttributes>builder()
                                               .output(inputStream)
                                               .mediaType(org.mule.runtime.api.metadata.MediaType.parse("application/pdf"))
                                               .attributes(attributes)
                                               .build());

                        pdfPartsCounter++;
                        inputStream.close();
                    } catch (Exception e) {
                        LOGGER.error("Exception " + e.getMessage() + " occurred. Closing the pdfDocumentPart.");
                    } finally {
                        try {
                            pdfDocumentPart.close();
                        } catch (IOException ioException) {
                            LOGGER.warn("Failed to close pdfDocumentPart: " + ioException.getMessage(), ioException);
                        }
                    }
                }
            } else {
                LOGGER.info( fileName + " has fewer pages than the " + maxPages + " configured in the Split Document operation.");

                PdfAttributes attributes = new PdfAttributes();
                attributes.setPageCount(totalPages);
                attributes.setFileName(fileName);
                pdfPartsList.add(Result.<InputStream, PdfAttributes>builder()
                                       .output(pdfPayload)
                                       .mediaType(org.mule.runtime.api.metadata.MediaType.parse("application/pdf"))
                                       .attributes(attributes)
                                       .build());
            }
        }

        return pdfPartsList;

    }

}
