package de.matrixweb.smaller.coffeescript;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import de.matrixweb.smaller.javascript.JavaScriptExecutor;
import de.matrixweb.smaller.javascript.JavaScriptExecutorFast;
import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.Type;
import de.matrixweb.smaller.resource.vfs.VFS;
import de.matrixweb.smaller.resource.vfs.VFSUtils;
import de.matrixweb.smaller.resource.vfs.VFile;

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
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.smaller.resource.vfs.VFS,
   *      de.matrixweb.smaller.resource.Resource, java.util.Map)
   */
  @Override
  public Resource execute(final VFS vfs, final Resource resource,
      final Map<String, String> options) throws IOException {
    if (!resource.getPath().endsWith(".coffee")) {
      return resource;
    }

    final VFile target = vfs.find(FilenameUtils.removeExtension(resource
        .getPath()) + ".js");
    final Writer writer = VFSUtils.createWriter(target);
    try {
      this.executor.run(new StringReader(resource.getContents()), writer);
    } finally {
      IOUtils.closeQuietly(writer);
    }
    return resource.getResolver().resolve(target.getPath());
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#dispose()
   */
  @Override
  public void dispose() {
    this.executor.dispose();
  }

}
