package com.sinnerschrader.smaller.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.sinnerschrader.smaller.resource.Processor;
import com.sinnerschrader.smaller.resource.Resource;
import com.sinnerschrader.smaller.resource.ResourceResolver;
import com.sinnerschrader.smaller.resource.Type;

/**
 * @author marwol
 */
public class ServletContextResourceResolver implements ResourceResolver {

  private final ServletContext context;

  /**
   * @param context
   */
  public ServletContextResourceResolver(final ServletContext context) {
    this.context = context;
  }

  /**
   * @see com.sinnerschrader.smaller.resource.ResourceResolver#resolve(java.lang.String)
   */
  @Override
  public Resource resolve(final String path) {
    return new ServletContextResource(this, this.context, path);
  }

  /** */
  public static class ServletContextResource implements Resource {

    private final ResourceResolver resolver;
    
    private final ServletContext context;

    private final String path;

    /**
     * @param context
     * @param path
     */
    public ServletContextResource(final ResourceResolver resolver, final ServletContext context, final String path) {
      this.resolver = resolver;
      this.context = context;
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
      InputStream in = this.context.getResourceAsStream(this.path);
      try {
        return IOUtils.toString(in);
      } finally {
        IOUtils.closeQuietly(in);
      }
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
