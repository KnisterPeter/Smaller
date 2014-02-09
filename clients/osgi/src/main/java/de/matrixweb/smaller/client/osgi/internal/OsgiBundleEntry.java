package de.matrixweb.smaller.client.osgi.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.osgi.framework.Bundle;

import de.matrixweb.vfs.wrapped.WrappedSystem;

/**
 * @author markusw
 */
public class OsgiBundleEntry implements WrappedSystem {

  private final Bundle bundle;

  private final String path;

  /**
   * @param bundle
   * @param path
   */
  public OsgiBundleEntry(final Bundle bundle, final String path) {
    this.bundle = bundle;
    this.path = path;
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

    final Enumeration<String> entries = this.bundle.getEntryPaths(this.path);
    if (entries != null) {
      while (entries.hasMoreElements()) {
        final String entry = entries.nextElement();
        final String[] parts = entry.split("/", 3);
        list.add(new OsgiBundleEntry(this.bundle, parts[0] + '/' + parts[1]));
      }
    }

    return list;
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
