package org.mule.extension.pdf.internal.operation;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Instant;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.mule.extension.pdf.api.PdfAttributes;
import org.mule.extension.pdf.internal.metadata.BinaryMetadataResolver;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.slf4j.Logger;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.util.XRLog;

public class HtmlToPdfOperation {
    private static final Logger LOGGER = getLogger(HtmlToPdfOperation.class);

    @MediaType("application/pdf")
    @OutputResolver(output = BinaryMetadataResolver.class)
    @DisplayName("HTML to PDF")
    public void htmlToPdf(
            @DisplayName("HTML Payload")
                @Content InputStream htmlPayload,
            @DisplayName("Output File Name") @Summary("The file name of the PDF Document we will produce.")
                String outputPdfFileName,
            CompletionCallback<InputStream, PdfAttributes> callback) throws Exception {

        Long epochStart = Instant.now().toEpochMilli();

        Document htmlDocument = Jsoup.parse(htmlPayload, "UTF-8", "");

        // Configure HTML Output to comply with XHTML
        htmlDocument.outputSettings()
                    .syntax(Document.OutputSettings.Syntax.xml)
                    .charset("UTF-8")
                    .escapeMode(Entities.EscapeMode.xhtml);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            //XRLog.setLevel(XRLog.GENERAL, Level.SEVERE); //setting it to avoid css warnings in log
            XRLog.listRegisteredLoggers().forEach(logger -> XRLog.setLevel(logger, java.util.logging.Level.SEVERE));
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(htmlDocument.selectFirst("html").outerHtml(), null);
            builder.toStream(outputStream);
            builder.run();

            PdfAttributes responsePdfAttributes = new PdfAttributes();
            responsePdfAttributes.setFileName(outputPdfFileName);

            InputStream responsePdfContent = new ByteArrayInputStream(outputStream.toByteArray());

            LOGGER.info("HTML to PDF completed in " + (Instant.now().toEpochMilli() - epochStart) + "ms.");

            callback.success(Result.<InputStream, PdfAttributes>builder()
                    .output(responsePdfContent)
                    .mediaType(org.mule.runtime.api.metadata.MediaType.parse("application/pdf"))
                    .attributes(responsePdfAttributes)
                    .build());

        } catch (Exception e) {
            callback.error(e);
        }
    }
}
