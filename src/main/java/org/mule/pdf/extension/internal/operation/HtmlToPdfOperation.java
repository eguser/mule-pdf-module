package org.mule.pdf.extension.internal.operation;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.time.Instant;

import javax.inject.Inject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.mule.pdf.extension.api.PDFAttributes;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.slf4j.Logger;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

public class HtmlToPdfOperation {
    private static final Logger LOGGER = getLogger(HtmlToPdfOperation.class);

    @Inject
    private SchedulerService schedulerService;

    @MediaType(value = MediaType.ANY, strict = false)
    @DisplayName("HTML to PDF")
    public void htmlToPdf(
            //@Config PDFModuleConfiguration config,
            @DisplayName("HTML Payload")
                @Content //@TypeResolver(BinaryMetadataResolver.class)
                InputStream htmlPayload,
            @DisplayName("Output File Name") @Summary("The file name of the PDF Document we will produce.")
                String outputPdfFileName,
            StreamingHelper streamingHelper,
            CompletionCallback<InputStream, PDFAttributes> callback) throws Exception {

        Long epochStart = Instant.now().toEpochMilli();

        Document htmlDocument = Jsoup.parse(htmlPayload, "UTF-8", "");

        // Configure HTML Output to comply with XHTML
        htmlDocument.outputSettings()
                    .syntax(Document.OutputSettings.Syntax.xml)
                    .charset("UTF-8")
                    .escapeMode(Entities.EscapeMode.xhtml);

        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream responsePdfContent = new PipedInputStream(outputStream);
        //ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //InputStream responsePdfContent = new ByteArrayInputStream(outputStream.toByteArray());

        schedulerService.ioScheduler().submit(() -> {
            try ( outputStream ) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.withHtmlContent(htmlDocument.selectFirst("html").outerHtml(), null);
                builder.toStream(outputStream);
                builder.run();
            } catch (Exception e) {
                callback.error(e);
            }
        });

        PDFAttributes responsePdfAttributes = new PDFAttributes();
        responsePdfAttributes.setFileName(outputPdfFileName);

        LOGGER.info("HTML to PDF completed in " + (Instant.now().toEpochMilli() - epochStart));
        System.out.println("HTML to PDF completed in " + (Instant.now().toEpochMilli() - epochStart));

        callback.success(Result.<InputStream, PDFAttributes>builder()
                .output(responsePdfContent)
                .mediaType(org.mule.runtime.api.metadata.MediaType.parse("application/pdf"))
                .attributes(responsePdfAttributes)
                .build());
    }
}
