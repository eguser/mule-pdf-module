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
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.pdf.extension.api.PDFAttributes;
import org.mule.pdf.extension.internal.config.PDFModuleConfiguration;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.slf4j.Logger;

public class SplitDocumentOperation {
	private static final Logger LOGGER = getLogger(SplitDocumentOperation.class);
	
    @MediaType(value = MediaType.ANY, strict = false)
    @DisplayName("Split Document")
    public List<Result<InputStream, PDFAttributes>> splitDocument(
    		@Config PDFModuleConfiguration config,
    		@DisplayName("Original File Name") @Summary("The file name of the PDF Document we will split.")
    			String fileName,
    		@DisplayName("Original PDF Payload")
            	InputStream pdfPayload,
            @DisplayName("Split Label Suffix") @Example("Part")
            	String splitLabel,
            @DisplayName("File Name and Suffix Separator") @Example("_")
            	String labelSeparator,
            @DisplayName("Max Number of Pages in Each Part")
            	int maxPages,
            StreamingHelper streamingHelper) throws Exception {
    	
        List<Result<InputStream, PDFAttributes>> pdfPartsList = new ArrayList<>();
		
        String partFileName = FilenameUtils.removeExtension(fileName) + labelSeparator + splitLabel;

        Splitter splitter = new Splitter();
        splitter.setSplitAtPage(maxPages);
		
        try (PDDocument originalPdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(pdfPayload))){

            int totalPages = originalPdfDocument.getNumberOfPages();
            int pdfPartsCounter = 0;
            
            if (totalPages > maxPages) {
            	List<PDDocument> originalPdfDocumentParts = splitter.split(originalPdfDocument);

            	for (PDDocument pdfDocumentPart : originalPdfDocumentParts) {
                    try ( ByteArrayOutputStream outputStream = new ByteArrayOutputStream() ) {
                        pdfDocumentPart.save(outputStream);
            			
                        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

                        // Collect metadata
                        PDFAttributes attributes = new PDFAttributes();
                        attributes.setPageCount(pdfDocumentPart.getNumberOfPages());
                        attributes.setFileSize(outputStream.size());
                        attributes.setFileName(partFileName + String.format("%03d",pdfPartsCounter) + ".pdf");
                        
                        pdfPartsList.add(Result.<InputStream, PDFAttributes>builder()
                                               .output(inputStream)
                                               .mediaType(org.mule.runtime.api.metadata.MediaType.parse("application/pdf"))
                                               .attributes(attributes)
                                               .build());

                        pdfPartsCounter++;
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

                PDFAttributes attributes = new PDFAttributes();
                attributes.setPageCount(totalPages);
                attributes.setFileName(fileName);
                pdfPartsList.add(Result.<InputStream, PDFAttributes>builder()
                                       .output(pdfPayload)
                                       .mediaType(org.mule.runtime.api.metadata.MediaType.parse("application/pdf"))
                                       .attributes(attributes)
                                       .build());
            }
        }

        return pdfPartsList;

    }

}
