package de.matrixweb.smaller.resource.vfs.wrapped;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.matrixweb.smaller.resource.vfs.VFile;

/**
 * @author markusw
 */
public class WrappedVFS implements WrappedSystem {

  private final VFile file;

  /**
   * @param file
   */
  public WrappedVFS(final VFile file) {
    this.file = file;
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.wrapped.WrappedSystem#getName()
   */
  @Override
  public String getName() {
    return this.file.getName();
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.wrapped.WrappedSystem#exists()
   */
  @Override
  public boolean exists() {
    return this.file.exists();
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.wrapped.WrappedSystem#isDirectory()
   */
  @Override
  public boolean isDirectory() {
    return this.file.isDirectory();
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.wrapped.WrappedSystem#list()
   */
  @Override
  public List<WrappedSystem> list() {
    final List<WrappedSystem> list = new ArrayList<WrappedSystem>();
    for (final VFile child : this.file.getChildren()) {
      list.add(new WrappedVFS(child));
    }
    return list;
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.wrapped.WrappedSystem#lastModified()
   */
  @Override
  public long lastModified() {
    return this.file.getLastModified();
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.wrapped.WrappedSystem#getInputStream()
   */
  @Override
  public InputStream getInputStream() throws IOException {
    return this.file.getInputStream();
  }

}
