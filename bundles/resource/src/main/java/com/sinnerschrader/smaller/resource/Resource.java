package com.sinnerschrader.smaller.resource;

import java.io.IOException;
import java.net.URL;

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
   * @return The resource path
   */
  String getPath();

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
   * @return Returns the processed {@link Resource} (could be the same instance)
   * @throws IOException
   */
  Resource apply(Processor processor) throws IOException;

}
