package de.matrixweb.smaller.client.osgi.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osgi.framework.Bundle;

import de.matrixweb.vfs.scanner.ResourceLister;
import de.matrixweb.vfs.scanner.ResourceScanner;
import de.matrixweb.vfs.wrapped.WrappedSystem;

/**
 * @author markusw
 */
public class OsgiBundleEntry implements WrappedSystem {

  private final Bundle bundle;

  private final String path;

  private final String[] includes;

  private final String[] excludes;

  /**
   * @param bundle
   * @param path
   * @param includes
   * @param excludes
   */
  public OsgiBundleEntry(final Bundle bundle, final String path,
      final String[] includes, final String[] excludes) {
    this.bundle = bundle;
    this.path = path;
    this.includes = includes;
    this.excludes = excludes;
  }

  /**
   * @see de.matrixweb.vfs.wrapped.WrappedSystem#getName()
   */
  public String getName() {
    return this.path.substring(this.path.lastIndexOf('/') + 1,
        this.path.length());
  }

  /**
   * @see de.matrixweb.vfs.wrapped.WrappedSystem#exists()
   */
  public boolean exists() {
    return this.bundle.getEntry(this.path) != null;
  }

  /**
   * @see de.matrixweb.vfs.wrapped.WrappedSystem#isDirectory()
   */
  public boolean isDirectory() {
    final String folderPath = this.path.startsWith("/") ? this.path
        .substring(1) : this.path;
    final Enumeration<String> entries = this.bundle.getEntryPaths(this.path);
    if (entries != null) {
      while (entries.hasMoreElements()) {
        final String entry = entries.nextElement();
        final String folder = entry.split("/", 2)[0];
        if (folder.equals(folderPath)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * @see de.matrixweb.vfs.wrapped.WrappedSystem#list()
   */
  public List<WrappedSystem> list() {
    final List<WrappedSystem> list = new ArrayList<WrappedSystem>();

    final List<String> candidates = new ArrayList<String>();
    final Enumeration<String> entries = this.bundle.getEntryPaths(this.path);
    if (entries != null) {
      while (entries.hasMoreElements()) {
        final String entry = entries.nextElement();
        final String[] parts = entry.split("/", 3);
        candidates.add(parts[1]);
        // list.add(new OsgiBundleEntry(this.bundle, parts[0] + '/' + parts[1],
        // this.includes, this.excludes));
      }
    }
    for (final String filtered : filter(candidates)) {
      list.add(new OsgiBundleEntry(this.bundle, this.path + '/' + filtered,
          this.includes, this.excludes));
    }

    return list;
  }

  private Set<String> filter(final List<String> entries) {
    return new ResourceScanner(new ResourceLister() {
      public Set<String> list(final String path) {
        return new HashSet<String>(entries);
      }
    }, this.includes, this.excludes).getResources();
  }

  /**
   * @see de.matrixweb.vfs.wrapped.WrappedSystem#lastModified()
   */
  public long lastModified() {
    final URL url = this.bundle.getEntry(this.path);
    try {
      return url.openConnection().getLastModified();
    } catch (final IOException e) {
      return -1;
    }
  }

  /**
   * @see de.matrixweb.vfs.wrapped.WrappedSystem#getInputStream()
   */
  public InputStream getInputStream() throws IOException {
    return this.bundle.getEntry(this.path).openStream();
  }

}
