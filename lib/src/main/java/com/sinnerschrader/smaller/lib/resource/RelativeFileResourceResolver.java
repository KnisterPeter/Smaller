package com.sinnerschrader.smaller.lib.resource;

import java.io.File;


/**
 * @author marwol
 */
public class RelativeFileResourceResolver extends FileResourceResolver {

  private final String base;

  /**
   * @param base
   */
  public RelativeFileResourceResolver(final String base) {
    super();
    this.base = base;
  }

  /**
   * 
   */
  @Override
  public Resource resolve(final String path) {
    return new RelativeFileResource(this.base, path);
  }

  /** */
  public static class RelativeFileResource extends FileResource {

    private final String base;

    /**
     * @param base
     * @param path
     */
    public RelativeFileResource(final String base, final String path) {
      super(path);
      this.base = base;
    }

    /**
     * @see com.sinnerschrader.smaller.lib.resource.Resource#getPath()
     */
    @Override
    public String getPath() {
      String path = super.getPath();
      if (path.startsWith(this.base)) {
        return path;
      } else {
        return new File(this.base, path).getAbsolutePath();
      }
    }

  }

}