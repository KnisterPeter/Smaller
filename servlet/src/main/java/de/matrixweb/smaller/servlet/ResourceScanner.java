package de.matrixweb.smaller.servlet;

import java.util.Set;

import javax.servlet.ServletContext;

import de.matrixweb.vfs.scanner.ResourceLister;

/**
 * @author marwol
 */
public class ResourceScanner extends de.matrixweb.vfs.scanner.ResourceScanner {

  /**
   * @param servletContext
   * @param includes
   * @param excludes
   */
  public ResourceScanner(final ServletContext servletContext,
      final String[] includes, final String[] excludes) {
    super(new ResourceLister() {
      @Override
      @SuppressWarnings("unchecked")
      public Set<String> list(final String path) {
        return servletContext.getResourcePaths(path);
      }
    }, includes, excludes);
  }

}
