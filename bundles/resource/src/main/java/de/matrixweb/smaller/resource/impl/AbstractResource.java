package de.matrixweb.smaller.resource.impl;

import org.apache.commons.io.FilenameUtils;

import de.matrixweb.smaller.resource.Resource;
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
    final String ext = FilenameUtils.getExtension(getPath());
    for (final Type type : Type.values()) {
      if (type.isOfType(ext)) {
        return type;
      }
    }
    return Type.UNKNOWN;
  }

}
