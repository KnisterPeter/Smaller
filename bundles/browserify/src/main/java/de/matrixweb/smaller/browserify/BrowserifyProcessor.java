package de.matrixweb.smaller.browserify;

import java.io.IOException;
import java.util.Map;

import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.nodejs.NodejsExecutor;
import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.Type;

/**
 * @author marwol
 */
public class BrowserifyProcessor implements Processor {

  private NodejsExecutor node;

  /**
   * 
   */
  public BrowserifyProcessor() {
    this.node = new NodejsExecutor();
    try {
      this.node.addScriptFile(getClass(), "/browserify-2.33.1/index.js");
      this.node.addScriptFile(getClass(), "/browserify-2.33.1/browserify.js");
    } catch (IOException e) {
      throw new SmallerException("Failed to setup node for browserify", e);
    }
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
  public Resource execute(final Resource resource, final Map<String, String> options) throws IOException {
    return this.node.run(resource, options);
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
