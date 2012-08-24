package de.matrixweb.smaller.uglifyjs;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;


import de.matrixweb.smaller.javascript.JavaScriptExecutor;
import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.StringResource;
import de.matrixweb.smaller.resource.Type;

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
   * @see de.matrixweb.smaller.resource.Processor#supportsType(de.matrixweb.smaller.resource.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.JS;
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.smaller.resource.Resource)
   */
  @Override
  public Resource execute(final Resource resource) throws IOException {
    final StringWriter writer = new StringWriter();
    this.executor.run(new StringReader(resource.getContents()), writer);
    return new StringResource(resource.getResolver(), resource.getType(),
        resource.getPath(), writer.toString());
  }

}
