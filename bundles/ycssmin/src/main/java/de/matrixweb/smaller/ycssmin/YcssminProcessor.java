package de.matrixweb.smaller.ycssmin;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import de.matrixweb.smaller.javascript.JavaScriptExecutor;
import de.matrixweb.smaller.javascript.JavaScriptExecutorFast;
import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.StringResource;
import de.matrixweb.smaller.resource.Type;

/**
 * @author marwol
 */
public class YcssminProcessor implements Processor {

  private final JavaScriptExecutor executor;

  /**
   * 
   */
  public YcssminProcessor() {
    this.executor = new JavaScriptExecutorFast("ycssmin-913e1945c2", 9,
        getClass());
    this.executor.addScriptSource("var exports = {};", "rhino.js");
    this.executor.addScriptFile(getClass().getResource(
        "/ycssmin-913e1945c2/cssmin-913e1945c2.js"));
    this.executor.addCallScript("exports.cssmin(%s);");
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#supportsType(de.matrixweb.smaller.resource.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.CSS;
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.smaller.resource.Resource,
   *      java.util.Map)
   */
  @Override
  public Resource execute(final Resource resource,
      final Map<String, String> options) throws IOException {
    final StringWriter writer = new StringWriter();
    this.executor.run(new StringReader(resource.getContents()), writer);
    return new StringResource(resource.getResolver(), resource.getType(),
        resource.getPath(), writer.toString());
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#dispose()
   */
  @Override
  public void dispose() {
    this.executor.dispose();
  }

}
