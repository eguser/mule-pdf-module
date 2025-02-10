package org.mule.pdf.extension.internal.config;

import org.mule.pdf.extension.internal.operation.ExtractPagesOperation;
import org.mule.pdf.extension.internal.operation.SplitDocumentOperation;
import org.mule.pdf.extension.internal.param.ImageScanningParams;
import org.mule.pdf.extension.internal.param.SplitDocumentParams;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;

/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@Operations({SplitDocumentOperation.class, ExtractPagesOperation.class})
public class PDFModuleConfiguration {
  
  @ParameterGroup(name = "Image Scanning")
  private ImageScanningParams imageScanningParams;
  
  @ParameterGroup(name = "PDF Splitting")
  private SplitDocumentParams pdfSplittingParams;
  
  public ImageScanningParams getImageScaningParams() {
    return imageScanningParams;
  }
  
  public int getImageResolution() {
    return imageScanningParams.getImageResolution();    
  }
  
  public float getImageScanAreaFactor() {
    return imageScanningParams.getImageScanAreaFactor();
  }
  
  public SplitDocumentParams getPDFSplittingParams() {
    return pdfSplittingParams;
  }
}
