package de.matrixweb.smaller.resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import de.matrixweb.smaller.resource.impl.AbstractResource;
import de.matrixweb.smaller.resource.vfs.VFS;

/**
 * Implements a {@link ResourceResolver} which handles files system paths. If no
 * base directory is given all paths are prefixed with '/'.
 * 
 * @author marwol
 * @deprecated Use {@link de.matrixweb.smaller.resource.vfs.VFSResourceResolver}
 *             with
 *             {@link de.matrixweb.smaller.resource.vfs.VFS#mount(de.matrixweb.smaller.resource.vfs.VFile, File)}
 *             instead
 */
@Deprecated
public class FileResourceResolver implements ResourceResolver {

  private String root;

  private final String base;

  /**
   * 
   */
  public FileResourceResolver() {
    this("/");
  }

  /**
   * @param base
   */
  public FileResourceResolver(final String base) {
    if (base.endsWith("/")) {
      this.base = base;
    } else {
      this.base = base + "/";
    }
    this.root = this.base;
  }

  private FileResourceResolver(final String base, final String root) {
    this(base);
    this.root = root;
  }

  /**
   * @see de.matrixweb.smaller.resource.ResourceResolver#resolve(java.lang.String)
   */
  @Override
  public Resource resolve(final String path) {
    if (path == null) {
      return null;
    }
    return new FileResource(path.startsWith("/") ? new File(this.root, path)
        : new File(this.base, path), this.root);
  }

  /**
   * @see de.matrixweb.smaller.resource.ResourceResolver#writeAll()
   */
  @Override
  public File writeAll() throws IOException {
    final File file = File.createTempFile("smaller", ".temp");
    file.delete();
    file.mkdirs();
    FileUtils.copyDirectory(new File(this.root), file);
    return file;
  }

  /** */
  public static class FileResource extends AbstractResource {

    private final String root;

    private final File file;

    /**
     * @param file
     * @param root
     */
    public FileResource(final File file, final String root) {
      this.file = file;
      this.root = root;
    }

    /**
     * @see de.matrixweb.smaller.resource.Resource#getResolver()
     */
    @Override
    public ResourceResolver getResolver() {
      return new FileResourceResolver(this.file.getParent(), this.root);
    }

    /**
     * @see de.matrixweb.smaller.resource.Resource#getPath()
     */
    @Override
    public String getPath() {
      return this.file.getAbsolutePath();
    }

    /**
     * @see de.matrixweb.smaller.resource.Resource#getURL()
     */
    @Override
    public URL getURL() throws IOException {
      return new File(getPath()).toURI().toURL();
    }

    /**
     * @see de.matrixweb.smaller.resource.Resource#getContents()
     */
    @Override
    public String getContents() throws IOException {
      return FileUtils.readFileToString(this.file, "UTF-8");
    }

    /**
     * @see de.matrixweb.smaller.resource.Resource#apply(de.matrixweb.smaller.resource.vfs.VFS,
     *      de.matrixweb.smaller.resource.Processor, java.util.Map)
     */
    @Override
    public Resource apply(final VFS vfs, final Processor processor,
        final Map<String, String> options) throws IOException {
      return processor.execute(vfs, this, options);
    }

  }

}
