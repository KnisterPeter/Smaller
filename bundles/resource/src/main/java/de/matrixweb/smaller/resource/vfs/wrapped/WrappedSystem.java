package de.matrixweb.smaller.resource.vfs.wrapped;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * An abstract interface for several possible filesystems (e.g. native-fs,
 * zip-fs, ...).
 * 
 * @author markusw
 */
public interface WrappedSystem {

  /**
   * @return Returns the name if the wrapped resource
   */
  String getName();

  /**
   * @return Returns true if the wrapped resource exists, false otherwise
   */
  boolean exists();

  /**
   * @return Returns true if the wrapped resource is a directory, false
   *         otherwise
   */
  boolean isDirectory();

  /**
   * @return Returns a list of child resources of the wrapped resource
   */
  List<WrappedSystem> list();

  /**
   * @return Returns the timestamp when the wrapped resource was last modified
   */
  long lastModified();

  /**
   * @return Returns an {@link InputStream} to the wrapped resource
   * @throws IOException
   *           Thrown if the {@link InputStream} could not be created
   */
  InputStream getInputStream() throws IOException;

}
