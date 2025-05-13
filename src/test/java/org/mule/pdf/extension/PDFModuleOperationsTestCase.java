package org.mule.pdf.extension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

public class PDFModuleOperationsTestCase extends MuleArtifactFunctionalTestCase {

  /**
   * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
   */
  @Override
  protected String getConfigFile() {
    return "pdf-operations-config.xml";
  }

  @Test
  public void executeSplitDocumentOperation() throws Exception {
    String payloadValue = ((String) flowRunner("splitDocument").run()
                                      .getMessage()
                                      .getPayload()
                                      .getValue());
    assertThat(payloadValue, is("Hello Mariano Gonzalez!!!"));
  }

  @Test
  public void executeExtractPagesOperation() throws Exception {
    String payloadValue = ((String) flowRunner("extractPages")
                                      .run()
                                      .getMessage()
                                      .getPayload()
                                      .getValue());
    //assertThat(payloadValue, is("Using Configuration [configId] with Connection id [aValue:100]"));
    assertThat(payloadValue, is("Using Image Resolution of [150] DPI."));
  }
}
