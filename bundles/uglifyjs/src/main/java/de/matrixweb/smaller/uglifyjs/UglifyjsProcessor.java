package de.matrixweb.smaller.uglifyjs;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

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
public class UglifyjsProcessor implements Processor {

  private final JavaScriptExecutor executor;

  /**
   * 
   */
  public UglifyjsProcessor() {
    this.executor = new JavaScriptExecutorFast("uglify-1.3.3", 9, getClass());
    this.executor.addScriptSource("module = {};", "rhino.js");
    this.executor.addScriptFile(getClass().getResource(
        "/uglify-1.3.3/uglify-js.js"));
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
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.smaller.resource.vfs.VFS,
   *      de.matrixweb.smaller.resource.Resource, java.util.Map)
   */
  @Override
  public Resource execute(final VFS vfs, final Resource resource,
      final Map<String, String> options) throws IOException {
    return ProcessorUtil.process(vfs, resource, "js", new ProcessorCallback() {
      @Override
      public void call(final Reader reader, final Writer writer)
          throws IOException {
        UglifyjsProcessor.this.executor.run(reader, writer);
      }
    });
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#dispose()
   */
  @Override
  public void dispose() {
    this.executor.dispose();
  }

}
