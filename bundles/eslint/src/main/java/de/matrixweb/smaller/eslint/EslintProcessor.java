package de.matrixweb.smaller.eslint;

import java.io.IOException;
import java.util.Map;

import de.matrixweb.nodejs.NodeJsExecutor;
import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.resource.MultiResourceProcessor;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.Type;
import de.matrixweb.vfs.VFS;
import de.matrixweb.vfs.VFSUtils;

/**
 * @author markusw
 */
public class EslintProcessor implements MultiResourceProcessor {

  private final String version;

  private NodeJsExecutor node;

  /**
   * 
   */
  public EslintProcessor() {
    this("0.7.4");
  }

  /**
   * @param version
   */
  public EslintProcessor(final String version) {
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
        this.node.setModule(getClass(), "eslint-" + this.version, "eslint.js");
      } catch (final IOException e) {
        this.node = null;
        throw new SmallerException("Failed to setup node for eslint", e);
      }
    }
    final String result = this.node.run(vfs, null, options);
    final String content = VFSUtils.readToString(vfs.find(result)).trim();
    if (content.length() > 0) {
      //throw new JsHintException(content);
      throw new SmallerException(content);
    }
    return resource;
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
