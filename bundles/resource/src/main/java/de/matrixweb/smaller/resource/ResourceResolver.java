package de.matrixweb.smaller.resource;

import java.io.File;
import java.io.IOException;

/**
 * @author marwol
 */
public interface ResourceResolver {

  /**
   * @param path
   * @return Returns the resouce given by the path
   */
  Resource resolve(String path);

  /**
   * Writes all available resources to a temp directory and returns it.
   * 
   * @return Returns the temp directory the resources were written to
   * @throws IOException
   */
  File writeAll() throws IOException;

}
