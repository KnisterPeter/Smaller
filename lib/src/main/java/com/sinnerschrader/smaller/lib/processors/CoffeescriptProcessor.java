package com.sinnerschrader.smaller.lib.processors;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import com.sinnerschrader.smaller.lib.JavaScriptExecutor;
import com.sinnerschrader.smaller.lib.ProcessorChain.Type;
import com.sinnerschrader.smaller.lib.resource.Resource;
import com.sinnerschrader.smaller.lib.resource.StringResource;

/**
 * @author marwol
 */
public class CoffeescriptProcessor implements Processor {

  private JavaScriptExecutor executor;

  /**
   * 
   */
  public CoffeescriptProcessor() {
    executor = new JavaScriptExecutor("coffee-script-1.3.3");
    executor.addScriptFile("/coffee-script-1.3.3.js");
    executor.addCallScript("CoffeeScript.compile(%s)");
  }

  /**
   * @see com.sinnerschrader.smaller.lib.processors.Processor#supportsType(com.sinnerschrader.smaller.lib.ProcessorChain.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.JS;
  }

  /**
   * @see com.sinnerschrader.smaller.lib.processors.Processor#execute(com.sinnerschrader.smaller.lib.resource.Resource)
   */
  @Override
  public Resource execute(final Resource resource) throws IOException {
    if (!resource.getPath().endsWith(".coffee")) {
      return resource;
    }
    final StringWriter writer = new StringWriter();
    executor.run(new StringReader(resource.getContents()), writer);
    return new StringResource(resource.getResolver(), resource.getType(),
        resource.getPath(), writer.toString());
  }

}
