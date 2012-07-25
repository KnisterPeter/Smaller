package com.sinnerschrader.smaller;

import org.apache.commons.io.FilenameUtils;

import ro.isdc.wro.model.resource.ResourceType;

/**
 * @author marwol
 */
public final class Utils {

  private Utils() {
  }

  /**
   * @param in
   * @return Returns the {@link ResourceType} of the given input
   */
  public static ResourceType getResourceType(final String in) {
    final String ext = FilenameUtils.getExtension(in);
    if ("css".equals(ext) || "less".equals(ext) || "sass".equals(ext)) {
      return ResourceType.CSS;
    }
    return ResourceType.JS;
  }

}
