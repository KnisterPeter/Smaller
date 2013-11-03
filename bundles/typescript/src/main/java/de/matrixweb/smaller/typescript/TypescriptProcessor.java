package de.matrixweb.smaller.typescript;

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
import de.matrixweb.vfs.VFS;

/**
 * @author marwol
 */
public class TypescriptProcessor implements Processor {

  private final JavaScriptExecutor executor;

  private final ProcessorCallback callback = new ProcessorCallback() {
    @Override
    public void call(final Reader reader, final Writer writer)
        throws IOException {
      TypescriptProcessor.this.executor.run(reader, writer);
    }
  };

  /**
   * 
   */
  public TypescriptProcessor() {
    this.executor = new JavaScriptExecutorFast("typescript", -1, getClass());
    this.executor.addScriptFile(getClass().getResource("/typescript.js"));
    this.executor.addScriptFile(getClass().getResource("/typescript-env.js"));
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
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.vfs.VFS,
   *      de.matrixweb.smaller.resource.Resource, java.util.Map)
   */
  @Override
  public Resource execute(final VFS vfs, final Resource resource,
      final Map<String, String> options) throws IOException {
    // Version 1.1.0 handling
    if (Version.getVersion(options.get("version")).isAtLeast(Version._1_0_0)) {
      return ProcessorUtil.processAllFilesOfType(vfs, resource, "ts", "js",
          this.callback);
    }
    if (!resource.getPath().endsWith(".ts")) {
      return resource;
    }
    return ProcessorUtil.process(vfs, resource, "ts", "js", this.callback);
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#dispose()
   */
  @Override
  public void dispose() {
    this.executor.dispose();
  }

}
