package de.matrixweb.smaller.coffeescript;

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
public class CoffeescriptProcessor implements Processor {

  private final JavaScriptExecutor executor;

  /**
   * 
   */
  public CoffeescriptProcessor() {
    this("1.6.3");
  }

  /**
   * @param version
   */
  public CoffeescriptProcessor(final String version) {
    this.executor = new JavaScriptExecutorFast("coffee-script-" + version, -1,
        getClass());
    this.executor.addScriptFile(getClass().getResource(
        "/coffee-script-" + version + ".js"));
    this.executor.addScriptSource(
        "function compile(input) { return CoffeeScript.compile(input); }",
        "script");
    this.executor.addCallScript("compile(%s)");
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#supportsType(de.matrixweb.smaller.resource.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.JS;
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.smaller.resource.Resource,
   *      java.util.Map)
   */
  @Override
  public Resource execute(final Resource resource,
      final Map<String, String> options) throws IOException {
    if (!resource.getPath().endsWith(".coffee")) {
      return resource;
    }
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
