package de.matrixweb.smaller.browserify;

import java.io.IOException;
import java.util.Map;

import de.matrixweb.nodejs.NodeJsExecutor;
import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.resource.MergingProcessor;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.Type;
import de.matrixweb.vfs.VFS;
import de.matrixweb.vfs.VFile;

/**
 *
 */
public class BrowserifyProcessor implements MergingProcessor {

  private final String version;

  private NodeJsExecutor node;

  /**
   * 
   */
  public BrowserifyProcessor() {
    this("3.24.9");
  }

  /**
   * @param version
   */
  public BrowserifyProcessor(final String version) {
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
        this.node.setModule(getClass().getClassLoader(), "browserify-"
            + this.version, "browserify.js");
      } catch (final IOException e) {
        throw new SmallerException("Failed to setup node for browserify", e);
      }
    }
    final String outfile = this.node.run(vfs,
        resource != null ? resource.getPath() : null, options);
    if (outfile != null) {
      final VFile file = vfs.find('/' + outfile);
      if (!file.exists()) {
        throw new SmallerException("BrowserifyProcessor result does not exists");
      }
    }
    return resource == null || outfile == null ? resource : resource
        .getResolver().resolve('/' + outfile);
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#dispose()
   */
  @Override
  public void dispose() {
    if (this.node != null) {
      this.node.dispose();
      this.node = null;
    }
  }

}
