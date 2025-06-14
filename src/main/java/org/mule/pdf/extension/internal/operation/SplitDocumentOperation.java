package org.mule.pdf.extension.internal.operation;

import java.io.InputStream;

import org.mule.pdf.extension.api.PdfAttributes;
import org.mule.pdf.extension.internal.connection.PdfInternalConnection;
import org.mule.pdf.extension.internal.metadata.BinaryMetadataResolver;
import org.mule.pdf.extension.internal.operation.paging.SplitPdfPagingProvider;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.api.util.Preconditions;
import org.mule.runtime.extension.api.annotation.metadata.TypeResolver;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;

import com.typesafe.config.Optional;

public class SplitDocumentOperation {

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
        
        return new SplitPdfPagingProvider(pdfPayload, fileName, splitLabel, labelSeparator, maxPages, firstPages, streamingHelper);

        }

}
