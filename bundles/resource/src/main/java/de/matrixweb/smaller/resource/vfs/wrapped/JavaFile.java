package de.matrixweb.smaller.resource.vfs.wrapped;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author markusw
 */
public class JavaFile implements WrappedSystem {

  private final File file;

  /**
   * @param file
   */
  public JavaFile(final File file) {
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
    for (final File child : this.file.listFiles()) {
      list.add(new JavaFile(child));
    }
    return list;
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.wrapped.WrappedSystem#lastModified()
   */
  @Override
  public long lastModified() {
    return this.file.lastModified();
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.wrapped.WrappedSystem#getInputStream()
   */
  @Override
  public InputStream getInputStream() throws IOException {
    return new FileInputStream(this.file);
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.file.toString();
  }

}
