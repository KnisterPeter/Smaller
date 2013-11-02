package de.matrixweb.smaller.resource.vfs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.resource.vfs.internal.Root;
import de.matrixweb.smaller.resource.vfs.internal.VFSManager;
import de.matrixweb.smaller.resource.vfs.internal.VFileImpl;
import de.matrixweb.smaller.resource.vfs.wrapped.WrappedSystem;
import de.matrixweb.smaller.resource.vfs.wrapped.WrappedVFS;

/**
 * This implements a file system abstraction which is able to mount native
 * {@link File}s as read-only parts into the {@link VFS}. The native files are
 * never overwritten. To create a native filesystem from the virtual use
 * {@link #exportFS(File)}, to import a native filesystem use
 * {@link #importFS(File)}.
 * 
 * @author markusw
 */
public class VFS {

  private VFile root = new Root(this);

  private final String host;

  /**
   * 
   */
  public VFS() {
    this.host = VFSManager.register(this);
  }

  /**
   * 
   */
  public void dispose() {
    VFSManager.unregister(this);
  }

  /**
   * @param target
   *          The {@link VFile} to mount the native directory into
   * @param directory
   *          The resource directory to mount
   * @return Returns the {@link VFile} for the native directory
   */
  public VFile mount(final VFile target, final WrappedSystem directory) {
    if (!directory.isDirectory()) {
      throw new SmallerException(
          "Only directories cound be mounted in smaller vfs");
    }
    ((VFileImpl) target).mount(directory);
    return target;
  }

  /**
   * Stacks a new {@link VFS} root on-top of the current one. The result is a
   * new virtual file-system backed by the old one. The old one is read-only
   * afterwards.<br>
   * <b>Note: All current references to {@link VFile}s must be considered
   * outdated!</b>
   * 
   * @return Returns the root file of the old vfs status which could be used to
   *         rollback
   */
  public VFile stack() {
    final VFile oldroot = this.root;
    final WrappedSystem wrapped = new WrappedVFS(this.root);
    this.root = new Root(this);
    mount(this.root, wrapped);
    return oldroot;
  }

  /**
   * @param oldroot
   */
  public void rollback(final VFile oldroot) {
    this.root = oldroot;
  }

  /**
   * Returns a {@link VFile} for the given path. If there is no file at that
   * path, then a new one is returned.
   * 
   * @param path
   *          The path to the file
   * @return Returns the requested {@link VFile}.
   * @throws IOException
   */
  public VFile find(final String path) throws IOException {
    if (!path.startsWith("/")) {
      throw new IOException("VFS path find should start with '/'");
    }
    if ("/".equals(path)) {
      return this.root;
    }
    return this.root.find(path.substring(1));
  }

  /**
   * @param target
   * @throws IOException
   */
  public void exportFS(final File target) throws IOException {
    internalExportFS(target, this.root);
  }

  private void internalExportFS(final File target, final VFile file)
      throws IOException {
    if (file.isDirectory()) {
      for (final VFile dir : file.getChildren()) {
        internalExportFS(target, dir);
      }
    } else {
      final File targetFile = new File(target, file.getPath().substring(1));
      targetFile.getParentFile().mkdirs();
      final FileOutputStream out = new FileOutputStream(targetFile);
      try {
        FileUtils.write(targetFile, VFSUtils.readToString(file));
      } catch (final IOException e) {
        // Skip silently
      } finally {
        IOUtils.closeQuietly(out);
      }
    }
  }

  /**
   * @param source
   * @throws IOException
   */
  public void importFS(final File source) throws IOException {
    internalImportFS(source, source);
  }

  private void internalImportFS(final File source, final File file)
      throws IOException {
    if (file.isDirectory()) {
      for (final File dir : file.listFiles()) {
        internalImportFS(source, dir);
      }
    } else {
      final VFile targetFile = find(file.getAbsolutePath().substring(
          source.getAbsolutePath().length()));
      VFSUtils.write(targetFile, FileUtils.readFileToString(file, "UTF-8"));
    }
  }

  /**
   * @param file
   * @return Returns a {@link VFile} url
   */
  public URL createUrl(final VFile file) {
    try {
      return new URL("vfs://" + this.host + file.getPath());
    } catch (final MalformedURLException e) {
      throw new SmallerException("Failed to create valid URL", e);
    }
  }

}
