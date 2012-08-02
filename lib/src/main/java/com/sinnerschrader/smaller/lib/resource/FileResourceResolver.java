package com.sinnerschrader.smaller.lib.resource;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.sinnerschrader.smaller.lib.processors.Processor;

/**
 * @author marwol
 */
public class FileResourceResolver implements ResourceResolver {

  /**
   * @see com.sinnerschrader.smaller.lib.resource.ResourceResolver#resolve(java.lang.String)
   */
  @Override
  public Resource resolve(final String path) {
    return new FileResource(path);
  }

  /** */
  public static class FileResource implements Resource {

    private final String path;

    /**
     * @param path
     */
    public FileResource(final String path) {
      this.path = path;
    }

    /**
     * @see com.sinnerschrader.smaller.lib.resource.Resource#getType()
     */
    @Override
    public Type getType() {
      String ext = FilenameUtils.getExtension(this.path);
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
     * @see com.sinnerschrader.smaller.lib.resource.Resource#getPath()
     */
    @Override
    public String getPath() {
      return this.path;
    }

    /**
     * @see com.sinnerschrader.smaller.lib.resource.Resource#getContents()
     */
    @Override
    public String getContents() throws IOException {
      return FileUtils.readFileToString(new File(getPath()));
    }

    /**
     * @see com.sinnerschrader.smaller.lib.resource.Resource#apply(com.sinnerschrader.smaller.lib.processors.Processor)
     */
    @Override
    public Resource apply(final Processor processor) throws IOException {
      return processor.execute(this);
    }

  }

}
