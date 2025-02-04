package org.mule.pdf.extension.internal.param;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public class ImageScanningParams {
  
  @Parameter
  @DisplayName("Image Resolution (dpi)")
  //@Optional(defaultValue = "150")
  @Summary("Enter a value between 72 - 300. The higher the number the more performance will be impacted")
  private PageImageDPI imageResolution;
  
  @Parameter
  @DisplayName("Scanned Area Factor")
  @Optional(defaultValue = ".995")
  @Summary("Set the area of the page to be scanned for non-blank pixels")
  float imageScanAreaFactor;
  
  public int getImageResolution() {
    return imageResolution.dpiValue;
  }
  
  public float getImageScanAreaFactor() {
    return imageScanAreaFactor;
  }

}