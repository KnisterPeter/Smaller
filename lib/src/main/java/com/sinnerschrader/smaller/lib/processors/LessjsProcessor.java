package com.sinnerschrader.smaller.lib.processors;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import com.sinnerschrader.smaller.lib.JavaScriptExecutor;
import com.sinnerschrader.smaller.lib.ProcessorChain.Type;
import com.sinnerschrader.smaller.lib.resource.Resource;
import com.sinnerschrader.smaller.lib.resource.StringResource;

/**
 * @author markusw
 */
public class LessjsProcessor implements Processor {

  /**
   * @see com.sinnerschrader.smaller.lib.processors.Processor#supportsType(com.sinnerschrader.smaller.lib.ProcessorChain.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.CSS;
  }

  /**
   * @see com.sinnerschrader.smaller.lib.processors.Processor#execute(com.sinnerschrader.smaller.lib.resource.Resource)
   */
  @Override
  public Resource execute(final Resource resource) throws IOException {
    final StringWriter writer = new StringWriter();

    JavaScriptExecutor executor = new JavaScriptExecutor("less-1.3.0");
    executor.addProperty("resolver", resource.getResolver());
    executor.addScriptFile("/lessjs-1.3.0/less-env.js");
    executor.addScriptFile("/lessjs-1.3.0/less-1.3.0.js");
    executor.addCallScript("lessIt(%s);");
    executor.run(new StringReader(resource.getContents()), writer);

    return new StringResource(resource.getResolver(), resource.getType(),
        resource.getPath(), writer.toString());
  }

}
