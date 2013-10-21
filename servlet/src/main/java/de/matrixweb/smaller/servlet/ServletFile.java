package de.matrixweb.smaller.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.io.FilenameUtils;

import de.matrixweb.smaller.resource.vfs.wrapped.WrappedSystem;

/**
 * @author marwol
 */
public class ServletFile implements WrappedSystem {

  private final ServletContext context;

  private final String path;

  /**
   * @param context
   * @param path
   */
  public ServletFile(final ServletContext context, final String path) {
    this.context = context;
    this.path = path;
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.wrapped.WrappedSystem#getName()
   */
  @Override
  public String getName() {
    String name = this.path;
    if (name.endsWith("/")) {
      name = name.substring(0, name.length() - 1);
    }
    return FilenameUtils.getName(name);
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.wrapped.WrappedSystem#exists()
   */
  @Override
  public boolean exists() {
    URL url = null;
    try {
      url = this.context.getResource(this.path);
    } catch (final MalformedURLException e) {
      // Ignore this
    }
    return url != null;
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.wrapped.WrappedSystem#isDirectory()
   */
  @Override
  public boolean isDirectory() {
    return this.context.getResourcePaths(this.path).size() > 0;
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.wrapped.WrappedSystem#list()
   */
  @Override
  @SuppressWarnings("unchecked")
  public List<WrappedSystem> list() {
    final List<WrappedSystem> list = new ArrayList<WrappedSystem>();
    for (final String entry : (Set<String>) this.context
        .getResourcePaths(this.path)) {
      list.add(new ServletFile(this.context, entry));
    }
    return list;
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.wrapped.WrappedSystem#lastModified()
   */
  @Override
  public long lastModified() {
    try {
      final URL url = this.context.getResource(this.path);
      if (url != null) {
        return url.openConnection().getLastModified();
      }
    } catch (final MalformedURLException e) {
      // Ignore this
    } catch (final IOException e) {
      // Ignore this
    }
    return -1;
  }

  /**
   * @see de.matrixweb.smaller.resource.vfs.wrapped.WrappedSystem#getInputStream()
   */
  @Override
  public InputStream getInputStream() throws IOException {
    final InputStream in = this.context.getResourceAsStream(this.path);
    if (in == null) {
      throw new IOException("Failed to create input stream for " + this.path);
    }
    return in;
  }

}
