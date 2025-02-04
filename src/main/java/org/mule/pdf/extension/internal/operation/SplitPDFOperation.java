package org.mule.pdf.extension.internal.operation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.pdf.extension.api.PDFAttributes;
import org.mule.pdf.extension.internal.config.PDFModuleConfiguration;
import org.mule.runtime.extension.api.annotation.metadata.fixed.OutputJsonType;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;

public class SplitPDFOperation {
	
	@MediaType(value = MediaType.APPLICATION_JSON)
    @OutputJsonType(schema = "metadata/PDFSplitterOperationOutput.json")
    @DisplayName("Split PDF by Pages")
    public List<Result<InputStream, PDFAttributes>> splitPdf(
    		@Config PDFModuleConfiguration config,
            InputStream pdfInputStream, 
            @Optional(defaultValue = "1") int pagesPerSplit,
            StreamingHelper streamingHelper) throws Exception {
		
		List<Result<InputStream, PDFAttributes>> resultList = new ArrayList<>();
		
		try (PDDocument document = Loader.loadPDF(pdfInputStream.readAllBytes())) {
            int totalPages = document.getNumberOfPages();
            int splitCounter = 0;
            
            // Loop through and split the document
            for (int i = 0; i < totalPages; i += pagesPerSplit) {
                PDDocument splitDoc = new PDDocument();
                
                for (int j = 0; j < pagesPerSplit && (i + j) < totalPages; j++) {
                    splitDoc.addPage(document.getPage(i + j));
                }

                // Convert the split document to an InputStream
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                splitDoc.save(outputStream);
                InputStream splitStream = new ByteArrayInputStream(outputStream.toByteArray());
                
                // Collect metadata
                PDFAttributes attributes = new PDFAttributes();
                attributes.setPageCount(splitDoc.getNumberOfPages());
                attributes.setFileSize(outputStream.size());

                resultList.add(Result.<InputStream, PDFAttributes>builder()
                        .output(splitStream)
                        .attributes(attributes)
                        .build());
                
                splitDoc.close();
                splitCounter++;
            }
        }
		
		
		return resultList;
		
	}

}
