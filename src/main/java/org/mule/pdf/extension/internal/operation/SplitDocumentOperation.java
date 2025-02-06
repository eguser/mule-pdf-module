package org.mule.pdf.extension.internal.operation;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.pdf.extension.api.PDFAttributes;
import org.mule.pdf.extension.internal.config.PDFModuleConfiguration;
import org.mule.runtime.extension.api.annotation.metadata.fixed.OutputJsonType;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.slf4j.Logger;

public class SplitDocumentOperation {
	private static final Logger LOGGER = getLogger(SplitDocumentOperation.class);
	
	@MediaType(value = MediaType.APPLICATION_JSON)
    @OutputJsonType(schema = "metadata/SplitDocumentOperationOutput.json")
    @DisplayName("Split Document")
    public List<Result<InputStream, PDFAttributes>> splitDocument(
    		@Config PDFModuleConfiguration config,
    		@DisplayName("Original File Name") @Summary("The file name of the PDF Document we will split.")
    			String fileName,
    		@DisplayName("Original PDF Payload")
            	InputStream pdfPayload,
            @DisplayName("Split Label") @Example("Part")
            	String splitLabel,
            String labelSeparator,
            int maxPages,
            StreamingHelper streamingHelper) throws Exception {

		List<Result<InputStream, PDFAttributes>> pdfPartsList = new ArrayList<>();

		Splitter splitter = new Splitter();
		splitter.setSplitAtPage(maxPages);
		
		try (PDDocument originalPdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(pdfPayload))){

            int totalPages = originalPdfDocument.getNumberOfPages();
            int splitCounter = 0;
            
            if (totalPages > maxPages) {
            	List<PDDocument> originalPdfDocumentParts = splitter.split(originalPdfDocument);
            	
            	for (PDDocument pdfDocumentPart : originalPdfDocumentParts) {
            		try (
            				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();            			
                			InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            		) {
            			pdfDocumentPart.save(outputStream);
            			
            			// Collect metadata
                        PDFAttributes attributes = new PDFAttributes();
                        attributes.setPageCount(pdfDocumentPart.getNumberOfPages());
                        attributes.setFileSize(outputStream.size());
                        attributes.setFileName(fileName + labelSeparator + splitLabel + splitCounter);
                        
            			pdfPartsList.add(Result.<InputStream, PDFAttributes>builder()
            					.output(inputStream)
            					.attributes(null)
            					.build());
            			
            			pdfDocumentPart.close();
            		} catch (Exception e) {
						LOGGER.error("Exception " + e.getMessage() + " occurred. Closing the pdfDocumentPart.");
						pdfDocumentPart.close();
            		}
            	}
            } else {
            	LOGGER.info("originalPdfDocument has fewer pages than " + maxPages);
            	pdfPartsList.add(Result.<InputStream, PDFAttributes>builder()
    					.output(pdfPayload)
    					.attributes(null)
    					.build());
            }
        }
		
		return pdfPartsList;
		
	}

}
