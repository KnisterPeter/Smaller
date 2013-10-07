package de.matrixweb.smaller.resource;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * @author marwol
 */
public interface Resource {

  /**
   * @return Returns the {@link ResourceResolver} which resolved this
   *         {@link Resource}
   */
  ResourceResolver getResolver();

  /**
   * @return Returns the resource {@link Type}
   */
  Type getType();

  /**
   * @return The absolute resource path
   */
  String getPath();

  /**
   * @return The path relative to the 'working' directory
   */
  String getRelativePath();

  /**
   * @return Returns the resolved {@link URL} for this resource if possible,
   *         otherwise null (which does not mean the resources does not exists)
   * @throws IOException
   *           Thrown in case of problems to create a valid url
   */
  URL getURL() throws IOException;

  /**
   * @return Returns the resource content
   * @throws IOException
   */
  String getContents() throws IOException;

  /**
   * @param processor
   *          The {@link Processor} to apply to this resource
   * @param options
   *          A {@link Map} containing options for processing
   * @return Returns the processed {@link Resource} (could be the same instance)
   * @throws IOException
   */
  Resource apply(Processor processor, Map<String, String> options)
      throws IOException;

}
