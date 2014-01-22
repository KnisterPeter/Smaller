package de.matrixweb.smaller.ycssmin;

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
import de.matrixweb.vfs.VFS;

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
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.vfs.VFS,
   *      de.matrixweb.smaller.resource.Resource, java.util.Map)
   */
  @Override
  public Resource execute(final VFS vfs, final Resource resource,
      final Map<String, Object> options) throws IOException {
    return ProcessorUtil.process(vfs, resource, "css", new ProcessorCallback() {
      @Override
      public void call(final Reader reader, final Writer writer)
          throws IOException {
        YcssminProcessor.this.executor.run(reader, writer);
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
