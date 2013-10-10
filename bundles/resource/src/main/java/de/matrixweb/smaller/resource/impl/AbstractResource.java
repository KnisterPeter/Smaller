package de.matrixweb.smaller.resource.impl;

import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.ResourceUtil;
import de.matrixweb.smaller.resource.Type;

/**
 * @author marwol
 */
public abstract class AbstractResource implements Resource {

  /**
   * @see de.matrixweb.smaller.resource.Resource#getType()
   */
  @Override
  public Type getType() {
    return ResourceUtil.getType(getPath());
  }

}
