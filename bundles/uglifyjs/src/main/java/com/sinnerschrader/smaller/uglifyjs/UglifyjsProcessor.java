package com.sinnerschrader.smaller.uglifyjs;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import com.sinnerschrader.smaller.javascript.JavaScriptExecutor;
import com.sinnerschrader.smaller.resource.Processor;
import com.sinnerschrader.smaller.resource.Resource;
import com.sinnerschrader.smaller.resource.StringResource;
import com.sinnerschrader.smaller.resource.Type;

/**
 * @author marwol
 */
public class UglifyjsProcessor implements Processor {

  private final JavaScriptExecutor executor;

  /**
   * 
   */
  public UglifyjsProcessor() {
    this.executor = new JavaScriptExecutor("uglify-1.3.3");
    this.executor.addScriptSource("module = {};", "rhino.js");
    this.executor.addScriptFile("/uglify-1.3.3/uglify-js.js");
    this.executor.addCallScript("uglify(%s, {});");
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
    final StringWriter writer = new StringWriter();
    this.executor.run(new StringReader(resource.getContents()), writer);
    return new StringResource(resource.getResolver(), resource.getType(),
        resource.getPath(), writer.toString());
  }

}
