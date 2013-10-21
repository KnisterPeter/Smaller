package de.matrixweb.smaller.resource.vfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.ResourceResolver;
import de.matrixweb.smaller.resource.ResourceUtil;
import de.matrixweb.smaller.resource.Type;

/**
 * @author markusw
 */
public class VFSResourceResolver implements ResourceResolver {

  private final VFS vfs;

  /**
   * @param vfs
   */
  public VFSResourceResolver(final VFS vfs) {
    this.vfs = vfs;
  }

  /**
   * @see de.matrixweb.smaller.resource.ResourceResolver#resolve(java.lang.String)
   */
  @Override
  public Resource resolve(final String path) {
    try {
      return new VFSResource(this, this.vfs.find(path.startsWith("/") ? path
          : '/' + path));
    } catch (final IOException e) {
      throw new SmallerException("Failed to resolve '" + path + "'", e);
    }
  }

  /**
   * @see de.matrixweb.smaller.resource.ResourceResolver#writeAll()
   */
  @Override
  public File writeAll() throws IOException {
    throw new UnsupportedOperationException();
  }

  /** */
  public static class VFSResource implements Resource {

    private final VFSResourceResolver resolver;

    private final VFile file;

    private VFSResource(final VFSResourceResolver resolver, final VFile file) {
      this.resolver = resolver;
      this.file = file;
    }

    /**
     * @see de.matrixweb.smaller.resource.Resource#getResolver()
     */
    @Override
    public ResourceResolver getResolver() {
      return new ResourceResolver() {
        @Override
        public Resource resolve(final String path) {
          String abs = path;
          if (!abs.startsWith("/")) {
            abs = VFSResource.this.file.getParent().getPath() + abs;
          }
          return VFSResource.this.resolver.resolve(abs);
        }

        @Override
        public File writeAll() throws IOException {
          return VFSResource.this.resolver.writeAll();
        }
      };
    }

    /**
     * @see de.matrixweb.smaller.resource.Resource#getType()
     */
    @Override
    public Type getType() {
      return ResourceUtil.getType(this.file.getName());
    }

    /**
     * @see de.matrixweb.smaller.resource.Resource#getPath()
     */
    @Override
    public String getPath() {
      return this.file.getPath();
    }

    /**
     * @see de.matrixweb.smaller.resource.Resource#getURL()
     */
    @Override
    public URL getURL() throws IOException {
      return this.file.getURL();
    }

    /**
     * @see de.matrixweb.smaller.resource.Resource#getContents()
     */
    @Override
    public String getContents() throws IOException {
      final InputStream in = this.file.getInputStream();
      try {
        return IOUtils.toString(in, "UTF-8");
      } finally {
        IOUtils.closeQuietly(in);
      }
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

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return this.file.toString();
    }

  }

}
