package de.matrixweb.smaller.resource.vfs.internal;

import de.matrixweb.smaller.resource.vfs.VFS;
import de.matrixweb.smaller.resource.vfs.VFile;

/**
 * @author markusw
 */
public class Root extends VFileImpl {

  private final VFS vfs;

  /**
   * @param vfs
   */
  public Root(final VFS vfs) {
    super(null, "/", true);
    this.vfs = vfs;
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.VFile#getPath()
   */
  @Override
  public String getPath() {
    return "/";
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.VFile#getParent()
   */
  @Override
  public VFile getParent() {
    return this;
  }

  /**
   * @return the vfs
   */
  VFS getVFS() {
    return this.vfs;
  }

}
