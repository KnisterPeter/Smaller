package de.matrixweb.smaller.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.ResourceResolver;
import de.matrixweb.smaller.resource.Type;

/**
 * @author marwol
 */
public class ServletContextResourceResolver implements ResourceResolver {

  private final ServletContext context;

  private final String base;

  /**
   * @param context
   */
  public ServletContextResourceResolver(final ServletContext context) {
    this(context, "/");
  }

  /**
   * @param context
   * @param base
   */
  public ServletContextResourceResolver(final ServletContext context,
      final String base) {
    this.context = context;
    this.base = base;
  }

  /**
   * @see de.matrixweb.smaller.resource.ResourceResolver#resolve(java.lang.String)
   */
  @Override
  public Resource resolve(final String path) {
    if (path == null) {
      return null;
    }
    String full = path;
    if (!path.startsWith("/")) {
      full = this.base + full;
    }
    return new ServletContextResource(this.context, full);
  }

  /** */
  public static class ServletContextResource implements Resource {

    private final ServletContext context;

    private final String path;

    /**
     * @param context
     * @param path
     */
    public ServletContextResource(final ServletContext context,
        final String path) {
      this.context = context;
      this.path = path;
    }

    /**
     * @see de.matrixweb.smaller.resource.Resource#getResolver()
     */
    @Override
    public ResourceResolver getResolver() {
      return new ServletContextResourceResolver(this.context,
          FilenameUtils.getFullPath(this.path));
    }

    /**
     * @see de.matrixweb.smaller.resource.Resource#getType()
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
     * @see de.matrixweb.smaller.resource.Resource#getPath()
     */
    @Override
    public String getPath() {
      return this.path;
    }

    /**
     * @see de.matrixweb.smaller.resource.Resource#getURL()
     */
    @Override
    public URL getURL() throws IOException {
      return this.context.getResource(this.path);
    }

    /**
     * @see de.matrixweb.smaller.resource.Resource#getContents()
     */
    @Override
    public String getContents() throws IOException {
      final InputStream in = this.context.getResourceAsStream(this.path);
      try {
        return IOUtils.toString(in);
      } finally {
        IOUtils.closeQuietly(in);
      }
    }

    /**
     * @see de.matrixweb.smaller.resource.Resource#apply(de.matrixweb.smaller.resource.Processor,
     *      java.util.Map)
     */
    @Override
    public Resource apply(final Processor processor,
        final Map<String, String> options) throws IOException {
      return processor.execute(this, options);
    }

  }

}
