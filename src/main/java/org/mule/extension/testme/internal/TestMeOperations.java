package org.mule.extension.testme.internal;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.mule.extension.testme.internal.config.TestMeConfiguration;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;

import org.mule.runtime.extension.api.runtime.operation.Result;

/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class TestMeOperations {

  /**
   * Example of an operation that uses the configuration and a connection instance to perform some action.
   */
  @MediaType(value = ANY, strict = false)
  public String retrieveInfo(@Config TestMeConfiguration configuration//, 
                             //@Connection TestMeConnection connection
                             ){
    return "Using Image Resolution of [" + configuration.getImageResolution() + "] DPI.";
  }

  /**
   * Example of a simple operation that receives a string parameter and returns a new string message that will be set on the payload.
   */
  @MediaType(value = ANY, strict = false)
  public String sayHi(String person) {
    return buildHelloMessage(person);
  }
  
  @MediaType("application/pdf")
  public List<Result<InputStream, Void>> splitPDF(@Config TestMeConfiguration config ) {
    final List<Result<InputStream, Void>> listResult = new ArrayList<>();;
   
    
    return listResult;
  }

  /**
   * Private Methods are not exposed as operations
   */
  private String buildHelloMessage(String person) {
    return "Hello " + person + "!!!";
  }
}
