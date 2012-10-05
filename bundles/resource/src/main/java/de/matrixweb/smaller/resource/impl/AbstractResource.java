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
    if ("js".equals(ext) || "coffee".equals(ext)) {
      return Type.JS;
    } else if ("css".equals(ext) || "less".equals(ext)) {
      return Type.CSS;
    } else if ("json".equals(ext)) {
      return Type.JSON;
    }
    return Type.UNKNOWN;
  }

}
