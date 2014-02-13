package de.matrixweb.smaller.client.osgi.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

  private final Map<String, BundleInternal> files = new HashMap<String, OsgiBundleEntry.BundleInternal>();

  /**
   * @param bundle
   * @param path
   * @param includes
   * @param excludes
   */
  public OsgiBundleEntry(final Bundle bundle, final String path, final String[] includes, final String[] excludes) {
    this.bundle = bundle;
    this.path = path.startsWith("/") ? path : '/' + path;
    this.includes = includes;
    this.excludes = excludes;

    Enumeration<URL> urls = bundle.findEntries(path, null, true);
    if (urls != null) {
      while (urls.hasMoreElements()) {
        URL url = urls.nextElement();
        String entry = url.getPath();
        if (entry.endsWith("/")) {
          entry = entry.substring(0, entry.length() - 1);
        }
        this.files.put(entry, new BundleInternal(entry));
        int idx = entry.lastIndexOf('/');
        while (idx > 0) {
          entry = entry.substring(0, idx);
          if (!this.files.containsKey(entry)) {
            this.files.put(entry, new BundleInternal(entry));
          }
          idx = entry.lastIndexOf('/');
        }
      }
    }
  }

  /**
   * @see de.matrixweb.vfs.wrapped.WrappedSystem#getName()
   */
  public String getName() {
    return this.path.substring(this.path.lastIndexOf('/') + 1);
  }

  /**
   * @see de.matrixweb.vfs.wrapped.WrappedSystem#exists()
   */
  public boolean exists() {
    return exists(this.path);
  }

  private boolean exists(final String entry) {
    return this.files.containsKey(entry);
  }

  /**
   * @see de.matrixweb.vfs.wrapped.WrappedSystem#isDirectory()
   */
  public boolean isDirectory() {
    return isDirectory(this.path);
  }

  private boolean isDirectory(final String entry) {
    for (Entry<String, BundleInternal> file : this.files.entrySet()) {
      String filePath = file.getKey();
      if (filePath.startsWith(entry) && filePath.length() > entry.length()) {
        return true;
      }
    }
    return false;
  }

  /**
   * @see de.matrixweb.vfs.wrapped.WrappedSystem#list()
   */
  public List<WrappedSystem> list() {
    return list(this.path);
  }

  private List<WrappedSystem> list(final String entry) {
    List<WrappedSystem> list = new ArrayList<WrappedSystem>();

    Map<String, BundleInternal> candidates = new HashMap<String, BundleInternal>();
    for (Entry<String, BundleInternal> file : this.files.entrySet()) {
      String filePath = file.getKey();
      if (filePath.startsWith(entry)) {
        if (!"/".equals(entry)) {
          filePath = filePath.substring(entry.length());
        }
        String[] parts = filePath.split("/", 3);
        if (parts.length == 2) {
          candidates.put(parts[1], file.getValue());
        }
      }
    }
    list.addAll(filter(candidates));

    return list;
  }

  private Set<BundleInternal> filter(final Map<String, BundleInternal> candidates) {
    Set<BundleInternal> filtered = new HashSet<BundleInternal>();
    for (String selected : new ResourceScanner(new ResourceLister() {
      public Set<String> list(final String path) {
        return candidates.keySet();
      }
    }, this.includes, this.excludes).getResources()) {
      filtered.add(candidates.get(selected));
    }
    return filtered;
  }

  /**
   * @see de.matrixweb.vfs.wrapped.WrappedSystem#lastModified()
   */
  public long lastModified() {
    return lastModified(this.path);
  }

  private long lastModified(final String entry) {
    try {
      return this.bundle.getEntry(entry).openConnection().getLastModified();
    } catch (IOException e) {
      return -1;
    }
  }

  /**
   * @see de.matrixweb.vfs.wrapped.WrappedSystem#getInputStream()
   */
  public InputStream getInputStream() throws IOException {
    return getInputStream(this.path);
  }

  private InputStream getInputStream(final String entry) throws IOException {
    return this.bundle.getEntry(entry).openStream();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "[VFS-OSGiBundleEntry] " + this.bundle + ":" + this.path;
  }

  private class BundleInternal implements WrappedSystem {

    private final String path;

    /**
     * @param path
     */
    public BundleInternal(final String path) {
      this.path = path;
    }

    /**
     * @see de.matrixweb.vfs.wrapped.WrappedSystem#getName()
     */
    public String getName() {
      return this.path.substring(this.path.lastIndexOf('/') + 1);
    }

    /**
     * @see de.matrixweb.vfs.wrapped.WrappedSystem#exists()
     */
    public boolean exists() {
      return OsgiBundleEntry.this.exists(this.path);
    }

    /**
     * @see de.matrixweb.vfs.wrapped.WrappedSystem#isDirectory()
     */
    public boolean isDirectory() {
      return OsgiBundleEntry.this.isDirectory(this.path);
    }

    /**
     * @see de.matrixweb.vfs.wrapped.WrappedSystem#list()
     */
    public List<WrappedSystem> list() {
      return OsgiBundleEntry.this.list(this.path);
    }

    /**
     * @see de.matrixweb.vfs.wrapped.WrappedSystem#lastModified()
     */
    public long lastModified() {
      return OsgiBundleEntry.this.lastModified(this.path);
    }

    /**
     * @see de.matrixweb.vfs.wrapped.WrappedSystem#getInputStream()
     */
    public InputStream getInputStream() throws IOException {
      return OsgiBundleEntry.this.getInputStream(this.path);
    }

  }

}
