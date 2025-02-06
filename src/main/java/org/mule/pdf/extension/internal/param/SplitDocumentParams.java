package org.mule.pdf.extension.internal.param;

import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.*;

public class SplitDocumentParams {
  
  @Parameter
  @DisplayName("Max Pages per Document")
  @Summary("Maximum number of pages allowed in a single PDF Document")
  private int maxNumberOfPages;
  
  @Parameter
  @DisplayName("Max Size per Document")
  @Summary("Maximum Size allowed for a single PDF Document")
  private int maxDocumentSize;
  
  public int getMaxNumberOfPages() {
    return maxNumberOfPages;
  }
  
  public int getMaxDocumentSize() {
    return maxDocumentSize;
  }

}
