package de.matrixweb.smaller.resource;

import java.io.IOException;
import java.util.Map;

import de.matrixweb.smaller.resource.vfs.VFS;

/**
 * @author marwol
 */
public interface Processor {

  /**
   * @param type
   * @return True if the given type can be handled by this processor
   */
  boolean supportsType(Type type);

  /**
   * @param vfs
   *          The file system to operate in
   * @param resource
   * @param options
   *          A {@link Map} of options for processing
   * @return Returns the transformed source
   * @throws IOException
   */
  Resource execute(VFS vfs, Resource resource, Map<String, String> options)
      throws IOException;

  /**
   * 
   */
  void dispose();

}
