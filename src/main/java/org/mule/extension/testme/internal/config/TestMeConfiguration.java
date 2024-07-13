package org.mule.extension.testme.internal.config;

import org.mule.extension.testme.internal.TestMeOperations;
import org.mule.extension.testme.internal.param.ImageScanningParams;
import org.mule.extension.testme.internal.param.PDFSplittingParams;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;

/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@Operations(TestMeOperations.class)
public class TestMeConfiguration {
  
  @ParameterGroup(name = "Image Scanning")
  private ImageScanningParams imageScanningParams;
  
  @ParameterGroup(name = "PDF Splitting")
  private PDFSplittingParams pdfSplittingParams;
  
  public ImageScanningParams getImageScaningParams() {
    return imageScanningParams;
  }
  
  public int getImageResolution() {
    return imageScanningParams.getImageResolution();    
  }
  
  public float getImageScanAreaFactor() {
    return imageScanningParams.getImageScanAreaFactor();
  }
  
  public PDFSplittingParams getPDFSplittingParams() {
    return pdfSplittingParams;
  }
}
