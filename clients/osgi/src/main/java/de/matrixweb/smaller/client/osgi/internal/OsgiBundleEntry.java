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
import org.slf4j.LoggerFactory;

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

  private Map<String, BundleInternal> files = new HashMap<String, OsgiBundleEntry.BundleInternal>();

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

    Enumeration<URL> urls = bundle.findEntries(this.path, null, true);
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
    this.files = filter(this.files);
LoggerFactory.getLogger(OsgiBundleEntry.class).info("Files: " + files.keySet()  + " <= " + bundle);
  }
  
  private Map<String, BundleInternal> filter(final Map<String, BundleInternal> candidates) {
    Map<String, BundleInternal> filtered = new HashMap<String, OsgiBundleEntry.BundleInternal>();
    for (String selected : new ResourceScanner(new ResourceLister() {
        public Set<String> list(final String path) {
          Set<String> set = new HashSet<String>();
          for (Entry<String, BundleInternal> entry : files.entrySet()) {
            if (entry.getKey().startsWith(path) && !entry.getKey().substring(path.length()).contains("/")) {
              set.add(entry.getKey() + (entry.getValue().isDirectory() ? '/' : ""));
            }
          }
          return set;
        }
      }, this.includes, this.excludes).getResources()) {
        filtered.put(selected, candidates.get(selected));
      }
    if (filtered.size() > 0) {
      for(Entry<String, BundleInternal> entry : candidates.entrySet()) {
        if (entry.getValue().isDirectory()) {
          filtered.put(entry.getKey(), entry.getValue());
        }
      }
    }
    return filtered;
  }

  /**
   * @see de.matrixweb.vfs.wrapped.WrappedSystem#getName()
   */
  public String getName() {
    String name = path;
    if (path.endsWith("/")) {
      name = name.substring(0, name.length() - 1);
    }
    return name.substring(name.lastIndexOf('/') + 1);
  }

  /**
   * @see de.matrixweb.vfs.wrapped.WrappedSystem#exists()
   */
  public boolean exists() {
    return exists(this.path);
  }

  private boolean exists(final String entry) {
    return this.files.containsKey(entry) && this.bundle.getEntry(entry) != null;
  }

  /**
   * @see de.matrixweb.vfs.wrapped.WrappedSystem#isDirectory()
   */
  public boolean isDirectory() {
    return isDirectory(this.path);
  }

  private boolean isDirectory(final String entry) {
    if (files.isEmpty()) {
      return true;
    }
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
    return list0(this.path);
  }

  private List<WrappedSystem> list0(final String entry) {
    return new ArrayList<WrappedSystem>(getCandidates(entry).values());
  }
  
  private Map<String, BundleInternal> getCandidates(final String path) {
    Map<String, BundleInternal> candidates = new HashMap<String, BundleInternal>();
    for (Entry<String, BundleInternal> file : this.files.entrySet()) {
      String filePath = file.getKey();
      if (filePath.startsWith(path)) {
        filePath = filePath.substring(path.length());
        if (filePath.startsWith("/")) {
          filePath = filePath.substring(1);
        }
        if (filePath.length() > 0) {
          String[] parts = filePath.split("/");
          if (parts.length == 1) {
            candidates.put(path + parts[0], file.getValue());
          }
        }
      }
    }
    return candidates;
  }

  /**
   * @see de.matrixweb.vfs.wrapped.WrappedSystem#lastModified()
   */
  public long lastModified() {
    return lastModified(this.path);
  }

  private long lastModified(final String entry) {
    try {
LoggerFactory.getLogger(OsgiBundleEntry.class).info("Entry: " + entry + " <= " + bundle);
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
      return OsgiBundleEntry.this.list0(this.path);
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
    
    @Override
    public String toString() {
        return "[VFS-OSGiBundleEntry] " + bundle + ":" + this.path;
    }

  }

}
