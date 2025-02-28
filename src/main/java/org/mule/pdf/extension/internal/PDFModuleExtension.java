package org.mule.pdf.extension.internal;

import org.mule.pdf.extension.internal.operation.ExtractPagesOperation;
import org.mule.pdf.extension.internal.operation.SplitDocumentOperation;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.sdk.api.annotation.JavaVersionSupport;
import org.mule.sdk.api.meta.JavaVersion;


/**
 * This is the main class of an extension, is the entry point from which configurations, connection providers, operations
 * and sources are going to be declared.
 */
@Xml(prefix = "pdf")
@Extension(name = "PDF")
//@Configurations(PDFModuleConfiguration.class)
@JavaVersionSupport({JavaVersion.JAVA_8, JavaVersion.JAVA_11, JavaVersion.JAVA_17})
@Operations({SplitDocumentOperation.class, ExtractPagesOperation.class})
public class PDFModuleExtension {

}
