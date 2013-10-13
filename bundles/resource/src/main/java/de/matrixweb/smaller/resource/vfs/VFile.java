package de.matrixweb.smaller.resource.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

/**
 * This interface specifies the main entity in the smaller virtual file system
 * abstraction.
 * 
 * @author markusw
 */
public interface VFile {

  /**
   * Returns the filename.
   * 
   * @return Returns the filename
   */
  String getName();

  /**
   * Returns the path to this file starting at the virtual root.
   * 
   * @return Returns the path to the file
   */
  String getPath();

  /**
   * Returns a VFS {@link URL} for this {@link VFile}.
   * 
   * @return Returns the {@link URL} for this file
   */
  URL getURL();

  /**
   * Returns true if this file exists.
   * 
   * @return Returns true if this file exists, false otherwise
   */
  boolean exists();

  /**
   * Returns true if this file is a directory.
   * 
   * @return Returns true if this file is a directory, false otherwise
   */
  boolean isDirectory();

  /**
   * Returns this files parent directory.
   * 
   * @return Returns the parent file
   */
  VFile getParent();

  /**
   * Returns this files child files if any.
   * 
   * @return Returns the child files or an empty array.
   */
  List<VFile> getChildren();

  /**
   * Returns an {@link InputStream} to this files content for reading.
   * 
   * @return Returns an {@link InputStream} to this file
   * @throws IOException
   *           Thrown in case of errors
   */
  InputStream getInputStream() throws IOException;

  /**
   * Returns an {@link OutputStream} to this files content for writing. Writing
   * to the {@link OutputStream} overwrites the file.
   * 
   * @return Returns an {@link OutputStream} to this file
   * @throws IOException
   *           Thrown in case of errors
   */
  OutputStream getOutputStream() throws IOException;

  /**
   * Searches this files children for the given path.
   * 
   * @param path
   *          The path to a child
   * @return Returns a {@link VFile} reference to the result
   * @throws IOException
   *           Thrown in case of errors
   */
  VFile find(String path) throws IOException;

  /**
   * Creates this file as directory if not already existent.
   * 
   * @throws IOException
   *           Thrown in case of errors
   */
  void mkdir() throws IOException;

  /**
   * Returns the last modification date of the file.
   * 
   * @return Returns the last modification date
   */
  long getLastModified();

}
