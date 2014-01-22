package de.matrixweb.smaller.coffeescript;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import de.matrixweb.nodejs.NodeJsExecutor;
import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.Type;
import de.matrixweb.vfs.VFS;

/**
 * @author marwol
 */
public class CoffeescriptProcessor implements Processor {

  private final String version;

  private NodeJsExecutor node;

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
    if (this.node == null) {
      try {
        this.node = new NodeJsExecutor();
        this.node.setModule(getClass().getClassLoader(), "coffeescript-"
            + this.version, "coffeescript.js");
      } catch (final IOException e) {
        throw new SmallerException("Failed to setup node for coffeescript", e);
      }
    }
    final String outfile = this.node.run(vfs,
        resource != null ? resource.getPath() : null, options);
    Resource result = resource;
    if (resource != null) {
      if (outfile != null) {
        result = resource.getResolver().resolve(outfile);
      } else if (FilenameUtils.isExtension(resource.getPath(), "coffee")) {
        result = resource.getResolver().resolve(
            FilenameUtils.removeExtension(resource.getPath()) + ".js");
      }
    }
    return result;
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#dispose()
   */
  @Override
  public void dispose() {
    if (this.node != null) {
      this.node.dispose();
    }
  }

}
