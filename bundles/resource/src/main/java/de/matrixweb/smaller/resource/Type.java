package de.matrixweb.smaller.resource;

import java.util.Arrays;
import java.util.List;

/**
 * @author marwol
 */
public enum Type {

  /** */
  UNKNOWN,
  /** */
  JS("js", "coffee", "json", "ts"),
  /** */
  CSS("css", "less", "sass"),
  /** */
  IMAGE("jpeg", "jpg", "gif", "png"),
  /** */
  SVG("svg");

  private List<String> exts;

  private Type(final String... ext) {
    this.exts = Arrays.asList(ext);
  }

  /**
   * @param ext
   * @return Returns true if the given ext is contained in this type
   */
  public boolean isOfType(final String ext) {
    return this.exts.contains(ext);
  }

}
