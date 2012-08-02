package com.sinnerschrader.smaller.lib.resource;

import java.io.IOException;

/**
 * @author marwol
 */
public interface Resource {

  /**
   * @return Returns the resource {@link Type}
   */
  Type getType();

  /**
   * @return Returns the resource content
   * @throws IOException
   */
  String getContents() throws IOException;

}
