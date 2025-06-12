package org.mule.pdf.extension.internal;

import static org.mule.runtime.api.meta.Category.COMMUNITY;

import org.mule.pdf.extension.internal.connection.provider.PdfInternalConnectionProvider;
import org.mule.pdf.extension.internal.operation.ExtractPagesOperation;
import org.mule.pdf.extension.internal.operation.HtmlToPdfOperation;
import org.mule.pdf.extension.internal.operation.SplitDocumentOperation;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.sdk.api.annotation.JavaVersionSupport;
import org.mule.sdk.api.meta.JavaVersion;

/**
 * This is the main class of an extension, is the entry point from which
 * configurations, connection providers, operations
 * and sources are going to be declared.
 */
@Xml(prefix = "pdf")
@Extension(name = "PDF", category = COMMUNITY)
// @Configurations(PDFModuleConfiguration.class)
@ConnectionProviders(PdfInternalConnectionProvider.class)
@Operations({
        SplitDocumentOperation.class,
        ExtractPagesOperation.class,
        HtmlToPdfOperation.class
})
@JavaVersionSupport({ JavaVersion.JAVA_17 })
public class PdfModule {

}
