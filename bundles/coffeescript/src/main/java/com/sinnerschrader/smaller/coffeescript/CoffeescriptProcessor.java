package com.sinnerschrader.smaller.coffeescript;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

import com.sinnerschrader.smaller.common.SmallerException;
import com.sinnerschrader.smaller.javascript.JavaScriptExecutor;
import com.sinnerschrader.smaller.resource.Processor;
import com.sinnerschrader.smaller.resource.Resource;
import com.sinnerschrader.smaller.resource.StringResource;
import com.sinnerschrader.smaller.resource.Type;

/**
 * @author marwol
 */
public class CoffeescriptProcessor implements Processor {

  private final JavaScriptExecutor executor;

  /**
   * 
   */
  public CoffeescriptProcessor() {
    this.executor = new JavaScriptExecutor("coffee-script-1.3.3", -1);
    final InputStream is = getClass().getResourceAsStream(
        "/coffee-script-1.3.3.js");
    try {
      this.executor.addScriptFile(is, "/coffee-script-1.3.3.js");
    } catch (final IOException e) {
      throw new SmallerException("Failed to load coffee-script.js", e);
    } finally {
      IOUtils.closeQuietly(is);
    }
    this.executor.addCallScript("CoffeeScript.compile(%s)");
  }

  /**
   * @see com.sinnerschrader.smaller.resource.Processor#supportsType(com.sinnerschrader.smaller.resource.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.JS;
  }

  /**
   * @see com.sinnerschrader.smaller.resource.Processor#execute(com.sinnerschrader.smaller.resource.Resource)
   */
  @Override
  public Resource execute(final Resource resource) throws IOException {
    if (!resource.getPath().endsWith(".coffee")) {
      return resource;
    }
    final StringWriter writer = new StringWriter();
    this.executor.run(new StringReader(resource.getContents()), writer);
    return new StringResource(resource.getResolver(), resource.getType(),
        resource.getPath(), writer.toString());
  }

}
