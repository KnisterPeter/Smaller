package com.sinnerschrader.smaller.resource;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * @author marwol
 */
public class FileResourceResolver implements ResourceResolver {

  /**
   * @see com.sinnerschrader.smaller.resource.ResourceResolver#resolve(java.lang.String)
   */
  @Override
  public Resource resolve(final String path) {
    return new FileResource(this, path);
  }

  /** */
  public static class FileResource implements Resource {

    private final ResourceResolver resolver;

    private final String path;

    /**
     * @param resolver
     * @param path
     */
    public FileResource(final ResourceResolver resolver, final String path) {
      this.resolver = resolver;
      this.path = path;
    }

    /**
     * @see com.sinnerschrader.smaller.resource.Resource#getResolver()
     */
    @Override
    public ResourceResolver getResolver() {
      return this.resolver;
    }

    /**
     * @see com.sinnerschrader.smaller.resource.Resource#getType()
     */
    @Override
    public Type getType() {
      final String ext = FilenameUtils.getExtension(this.path);
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
      return this.path;
    }

    /**
     * @see com.sinnerschrader.smaller.resource.Resource#getContents()
     */
    @Override
    public String getContents() throws IOException {
      return FileUtils.readFileToString(new File(getPath()));
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
