package de.matrixweb.smaller.resource.vfs.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.matrixweb.smaller.resource.vfs.VFile;
import de.matrixweb.smaller.resource.vfs.wrapped.WrappedSystem;

/**
 * @author markusw
 */
public class VFileImpl implements VFile {

  private final VFileImpl parent;

  private final String name;

  private List<VFile> children = Collections.emptyList();

  private boolean directory;

  private int length = 0;

  private byte[] content;

  private long lastModified;

  private WrappedSystem wrapped;

  private boolean allNativeFilesWrapped = false;

  /**
   * @param parent
   * @param name
   * @param directory
   * @param wrapped
   */
  VFileImpl(final VFileImpl parent, final String name, final boolean directory,
      final WrappedSystem wrapped) {
    this(parent, name, directory);
    this.wrapped = wrapped;
  }

  VFileImpl(final VFileImpl parent, final String name, final boolean directory) {
    this.parent = parent;
    this.name = name;
    this.directory = directory;
    if (parent != null) {
      parent.addChild(this);
    }
  }

  /**
   * @param wrapped
   *          the wrapped to set
   */
  public void mount(final WrappedSystem wrapped) {
    this.wrapped = wrapped;
    this.directory = true;
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.VFile#getName()
   */
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.VFile#exists()
   */
  @Override
  public boolean exists() {
    return this.wrapped != null && this.wrapped.exists() || this.directory
        || this.length > 0;
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.VFile#isDirectory()
   */
  @Override
  public boolean isDirectory() {
    return this.directory;
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.VFile#getPath()
   */
  @Override
  public String getPath() {
    return this.parent.getPath() + getName() + (isDirectory() ? '/' : "");
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.VFile#getURL()
   */
  @Override
  public URL getURL() {
    return getRoot().getVFS().createUrl(this);
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.VFile#getParent()
   */
  @Override
  public VFile getParent() {
    return this.parent;
  }

  void addChild(final VFile file) {
    if (this.children == Collections.EMPTY_LIST) {
      this.children = new ArrayList<VFile>();
      this.directory = true;
    }
    if (!this.children.contains(file)) {
      this.children.add(file);
    }
  }

  void removeChild(final VFile file) {
    this.children.remove(file);
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.VFile#getChildren()
   */
  @Override
  public List<VFile> getChildren() {
    if (this.wrapped != null && !this.allNativeFilesWrapped) {
      final List<WrappedSystem> children = this.wrapped.list();
      for (final WrappedSystem child : children) {
        new VFileImpl(this, child.getName(), child.isDirectory(), child);
      }
      this.allNativeFilesWrapped = true;
    }
    return Collections.unmodifiableList(this.children);
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.VFile#getInputStream()
   */
  @Override
  public InputStream getInputStream() throws IOException {
    if (isDirectory()) {
      throw new IOException("Unable to read from directory");
    }
    if (!exists()) {
      throw new IOException("VFile '" + getPath() + "' does not exists");
    }
    if (this.wrapped != null && this.wrapped.lastModified() > this.lastModified) {
      return this.wrapped.getInputStream();
    }
    return new FileInputStream(this);
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.VFile#getOutputStream()
   */
  @Override
  public OutputStream getOutputStream() throws IOException {
    if (isDirectory()) {
      throw new IOException("Unable to write to directory");
    }
    return new FileOutputStream(this);
  }

  private Root getRoot() {
    VFile file = getParent();
    while (file != file.getParent()) {
      file = file.getParent();
    }
    return (Root) file;
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.VFile#find(java.lang.String)
   */
  @Override
  public VFile find(final String path) throws IOException {
    if (path.startsWith("/")) {
      return getRoot().find(path.substring(1));
    }
    final String[] parts = path.split("/", 2);
    VFile child = findVirtualChild(parts);
    if (child == null) {
      child = findWrappedChild(parts);
    }
    if (child == null) {
      // Create new virtual non-existing child
      child = new VFileImpl(this, parts[0], false);
    }
    if (parts.length > 1) {
      child = child.find(parts[1]);
    }
    return child;
  }

  private VFile findVirtualChild(final String[] parts) {
    VFile child = null;
    if (".".equals(parts[0])) {
      child = this;
    } else if ("..".equals(parts[0])) {
      child = getParent();
    } else {
      final Iterator<VFile> it = getChildren().iterator();
      while (child == null && it.hasNext()) {
        final VFile test = it.next();
        if (test.getName().equals(parts[0])) {
          child = test;
        }
      }
    }
    return child;
  }

  private VFile findWrappedChild(final String[] parts) {
    VFile child = null;
    if (this.wrapped != null && this.wrapped.isDirectory()) {
      final List<WrappedSystem> children = this.wrapped.list();
      for (int i = 0; child == null && i < children.size(); i++) {
        final WrappedSystem test = children.get(i);
        if (test.getName().equals(parts[0])) {
          child = new VFileImpl(this, parts[0], test.isDirectory(), test);
        }
      }

    }
    return child;
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.VFile#mkdir()
   */
  @Override
  public void mkdir() throws IOException {
    if (exists()) {
      throw new IOException("Already exists");
    }
    this.directory = true;
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.VFile#getLastModified()
   */
  @Override
  public long getLastModified() {
    return this.wrapped != null ? Math.max(this.wrapped.lastModified(),
        this.lastModified) : this.lastModified;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (this.name == null ? 0 : this.name.hashCode());
    result = prime * result
        + (this.parent == null ? 0 : this.parent.hashCode());
    return result;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final VFileImpl other = (VFileImpl) obj;
    if (this.name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!this.name.equals(other.name)) {
      return false;
    }
    if (this.parent == null) {
      if (other.parent != null) {
        return false;
      }
    } else if (!this.parent.equals(other.parent)) {
      return false;
    }
    return true;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getPath();
  }

  private static class FileInputStream extends InputStream {

    private final VFileImpl file;

    private int counter = 0;

    FileInputStream(final VFileImpl file) {
      this.file = file;
    }

    @Override
    public int read() throws IOException {
      if (this.file.content == null) {
        throw new IOException("none-existent");
      }
      if (this.counter == this.file.length) {
        return -1;
      }
      return this.file.content[this.counter++];
    }

  }

  private static class FileOutputStream extends OutputStream {

    private final VFileImpl file;

    private int length = 0;

    private byte[] data = new byte[1024];

    FileOutputStream(final VFileImpl file) {
      this.file = file;
    }

    private void extend(final int n) {
      if (this.length + n > this.data.length) {
        final byte[] temp = this.data;
        this.data = new byte[this.data.length + Math.max(n, 1024)];
        System.arraycopy(temp, 0, this.data, 0, this.length);
      }
    }

    @Override
    public void write(final int b) throws IOException {
      extend(1);
      this.data[this.length++] = (byte) b;
    }

    /**
     * @see java.io.OutputStream#write(byte[])
     */
    @Override
    public void write(final byte[] b) throws IOException {
      extend(b.length);
      System.arraycopy(b, 0, this.data, this.length, b.length);
      this.length += b.length;
    }

    /**
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    @Override
    public void write(final byte[] b, final int off, final int len)
        throws IOException {
      extend(len);
      System.arraycopy(b, off, this.data, this.length, len);
      this.length += len;
    }

    @Override
    public void close() throws IOException {
      this.file.length = this.length;
      this.file.content = new byte[this.length];
      System.arraycopy(this.data, 0, this.file.content, 0, this.length);
      this.file.lastModified = System.currentTimeMillis();
    }

  }

}
