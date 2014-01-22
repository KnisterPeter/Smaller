package de.matrixweb.smaller.uglifyjs;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import de.matrixweb.nodejs.NodeJsExecutor;
import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.javascript.JavaScriptExecutor;
import de.matrixweb.smaller.javascript.JavaScriptExecutorFast;
import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.ProcessorUtil;
import de.matrixweb.smaller.resource.ProcessorUtil.ProcessorCallback;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.Type;
import de.matrixweb.vfs.VFS;
import de.matrixweb.vfs.VFile;

/**
 * @author marwol
 */
public class UglifyjsProcessor implements Processor {

  private final String version;

  private JavaScriptExecutor executor;

  private NodeJsExecutor node;

  /**
   * 
   */
  public UglifyjsProcessor() {
    this("2.4.3");
  }

  /**
   * @param version
   */
  public UglifyjsProcessor(final String version) {
    this.version = version;
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
      final Map<String, Object> options) throws IOException {
    if (this.version.startsWith("2")) {
      return executeWithNode(vfs, resource, options);
    }
    return executeWithJs(vfs, resource, options);
  }

  private Resource executeWithNode(final VFS vfs, final Resource resource,
      final Map<String, Object> options) throws IOException {
    if (this.node == null) {
      this.node = new NodeJsExecutor();
      this.node.setModule(getClass().getClassLoader(), "uglifyjs-"
          + this.version);
    }

    final VFile infile = vfs.find(resource.getPath());
    if (!infile.exists()) {
      throw new SmallerException("Uglify input '" + infile
          + "' does not exists");
    }

    final String resultPath = this.node.run(vfs, resource.getPath(), options);
    final VFile outfile = vfs.find('/' + resultPath);
    if (!outfile.exists()) {
      throw new SmallerException("Uglify result '" + outfile
          + "' does not exists");
    }
    return resource.getResolver().resolve(outfile.getPath());
  }

  private Resource executeWithJs(final VFS vfs, final Resource resource,
      final Map<String, Object> options) throws IOException {
    if (this.executor == null) {
      this.executor = new JavaScriptExecutorFast("uglify-" + this.version, 9,
          getClass());
      this.executor.addScriptSource("module = {};", "rhino.js");
      this.executor.addScriptFile(getClass().getResource(
          "/uglify-" + this.version + "/uglify-js.js"));
      this.executor.addCallScript("uglify(%s, {});");
    }

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
    if (this.node != null) {
      this.node.dispose();
    }
    if (this.executor != null) {
      this.executor.dispose();
    }
  }

}
