package de.matrixweb.smaller.coffeescript;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import de.matrixweb.smaller.common.Version;
import de.matrixweb.smaller.javascript.JavaScriptExecutor;
import de.matrixweb.smaller.javascript.JavaScriptExecutorFast;
import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.ProcessorUtil;
import de.matrixweb.smaller.resource.ProcessorUtil.ProcessorCallback;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.Type;
import de.matrixweb.smaller.resource.vfs.VFS;

/**
 * @author marwol
 */
public class CoffeescriptProcessor implements Processor {

  private final JavaScriptExecutor executor;

  private final ProcessorCallback callback = new ProcessorCallback() {
    @Override
    public void call(final Reader reader, final Writer writer)
        throws IOException {
      CoffeescriptProcessor.this.executor.run(reader, writer);
    }
  };

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
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.smaller.resource.vfs.VFS,
   *      de.matrixweb.smaller.resource.Resource, java.util.Map)
   */
  @Override
  public Resource execute(final VFS vfs, final Resource resource,
      final Map<String, String> options) throws IOException {
    // Version 1.0.0 handling
    if (Version.getVersion(options.get("version")).isAtLeast(Version._1_0_0)) {
      return ProcessorUtil.processAllFilesOfType(vfs, resource, "coffee", "js",
          this.callback);
    }

    if (!resource.getPath().endsWith(".coffee")) {
      return resource;
    }
    return ProcessorUtil.process(vfs, resource, "coffee", "js", this.callback);
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#dispose()
   */
  @Override
  public void dispose() {
    this.executor.dispose();
  }

}
