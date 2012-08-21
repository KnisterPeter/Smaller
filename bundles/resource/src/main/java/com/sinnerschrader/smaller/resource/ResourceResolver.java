package com.sinnerschrader.smaller.resource;

/**
 * @author marwol
 */
public interface ResourceResolver {

  /**
   * @param path
   * @return Returns the resouce given by the path
   */
  Resource resolve(String path);

}
