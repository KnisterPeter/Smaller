package com.sinnerschrader.smaller.resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * Implements a {@link ResourceResolver} which handles files system paths. If no
 * base directory is given all paths are prefixed with '/'.
 * 
 * @author marwol
 */
public class FileResourceResolver implements ResourceResolver {

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
  }

  /**
   * @see com.sinnerschrader.smaller.resource.ResourceResolver#resolve(java.lang.String)
   */
  @Override
  public Resource resolve(final String path) {
    if (path == null) {
      return null;
    }
    return new FileResource(new File(this.base, path));
  }

  /** */
  public static class FileResource implements Resource {

    private final File file;

    /**
     * @param file
     */
    public FileResource(final File file) {
      this.file = file;
    }

    /**
     * @see com.sinnerschrader.smaller.resource.Resource#getResolver()
     */
    @Override
    public ResourceResolver getResolver() {
      return new FileResourceResolver(this.file.getParent());
    }

    /**
     * @see com.sinnerschrader.smaller.resource.Resource#getType()
     */
    @Override
    public Type getType() {
      final String ext = FilenameUtils.getExtension(this.file.getName());
      if ("js".equals(ext) || "coffee".equals(ext)) {
        return Type.JS;
      } else if ("css".equals(ext) || "less".equals(ext)) {
        return Type.CSS;
      } else if ("json".equals(ext)) {
        return Type.JSON;
      }
      return Type.UNKNOWN;
    }

    /**
     * @see com.sinnerschrader.smaller.resource.Resource#getPath()
     */
    @Override
    public String getPath() {
      return this.file.getAbsolutePath();
    }

    /**
     * @see com.sinnerschrader.smaller.resource.Resource#getURL()
     */
    @Override
    public URL getURL() throws IOException {
      return new File(getPath()).toURI().toURL();
    }

    /**
     * @see com.sinnerschrader.smaller.resource.Resource#getContents()
     */
    @Override
    public String getContents() throws IOException {
      return FileUtils.readFileToString(this.file);
    }

    /**
     * @see com.sinnerschrader.smaller.resource.Resource#apply(com.sinnerschrader.smaller.resource.Processor)
     */
    @Override
    public Resource apply(final Processor processor) throws IOException {
      return processor.execute(this);
    }

  }

}
