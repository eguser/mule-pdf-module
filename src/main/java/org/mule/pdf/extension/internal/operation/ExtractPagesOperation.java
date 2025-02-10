package org.mule.pdf.extension.internal.operation;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.metadata.TypeResolver;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.pdf.extension.api.PDFAttributes;
import org.mule.pdf.extension.internal.config.PDFModuleConfiguration;
import org.mule.pdf.extension.internal.metadata.BinaryMetadataResolver;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.slf4j.Logger;

public class ExtractPagesOperation {
	private static final Logger LOGGER = getLogger(ExtractPagesOperation.class);
	
	@MediaType(value = MediaType.ANY, strict = false)
    @DisplayName("Extract Pages")
    public Result<InputStream, PDFAttributes> extractPages(
    		@Config PDFModuleConfiguration config,
            @DisplayName("Original PDF Payload")
                @Content @TypeResolver(BinaryMetadataResolver.class) InputStream pdfPayload,
            @DisplayName("Original File Name") @Summary("The file name of the PDF Document we will extract pages from.")
                String fileName,
            @DisplayName("Extract Pages") @Summary("Use 1-based indexing for page numbers. i.e. 0 is not allowed.") 
    			@Example("3-6,8,10-13")
                String extractPages,
            StreamingHelper streamingHelper) throws Exception {
		

        String newFileName = FilenameUtils.removeExtension(fileName);
		try (
			PDDocument originalPdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(pdfPayload));
			PDDocument extractedPagesDocument = new PDDocument();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
		) {
			int totalPages = originalPdfDocument.getNumberOfPages();
			
			for (Integer pageIndex : getPageIndexes(extractPages)) {
				if (pageIndex < totalPages) {
					extractedPagesDocument.addPage(originalPdfDocument.getPage(pageIndex));
				}
			}
			
			extractedPagesDocument.save(outputStream);
			InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
			
			// Collect metadata
            PDFAttributes attributes = new PDFAttributes();
            attributes.setPageCount(extractedPagesDocument.getNumberOfPages());
            attributes.setFileSize(outputStream.size());
            attributes.setFileName(newFileName + "_extracted_" + ".pdf");
		
			return Result.<InputStream, PDFAttributes>builder()
	                     .output(inputStream)
	                     .mediaType(org.mule.runtime.api.metadata.MediaType.parse("application/pdf"))
	                     .attributes(attributes)
	                     .build();
		}
	}
	
	private List<Integer> getPageIndexes(String extractPages) {
		
		ArrayList<Integer> extractPageIndexes = new ArrayList<>();
		
		for (String desiredPageRange : extractPages.split(",") ) {
			if (desiredPageRange.contains("-")) {
				String[] range = desiredPageRange.split("-");
				int start = Integer.parseInt(range[0]) - 1 ;
				int end = Integer.parseInt(range[1]) - 1;
				for (int i = start; i <= end; i++ ) {
					extractPageIndexes.add(i);
				}
				
			} else {
				
				extractPageIndexes.add(Integer.parseInt(desiredPageRange) - 1 );
			}
		}
		return extractPageIndexes;
	}

}
