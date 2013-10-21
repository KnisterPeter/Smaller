package de.matrixweb.smaller.resource;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.matrixweb.smaller.resource.vfs.VFS;

/**
 * Just a wrapper to keep the {@link Resource} interface for multiple resources.
 * The only valid methods are {@link #getResources()}, {@link #getMerger()} and
 * {@link #apply(VFS, Processor, Map)}.
 *
 * @author markusw
 */
public class ResourceGroup implements Resource {

  private final List<Resource> resources;

  private final SourceMerger merger;

  /**
   * @param resources
   * @param merger
   */
  public ResourceGroup(final List<Resource> resources, final SourceMerger merger) {
    this.resources = new ArrayList<Resource>(resources);
    this.merger = merger;
  }

  /**
   * @return the resources
   */
  public List<Resource> getResources() {
    return this.resources;
  }

  /**
   * @return the merger
   */
  public SourceMerger getMerger() {
    return this.merger;
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#getResolver()
   */
  @Override
  public ResourceResolver getResolver() {
    throw new UnsupportedOperationException();
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#getType()
   */
  @Override
  public Type getType() {
    throw new UnsupportedOperationException();
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#getPath()
   */
  @Override
  public String getPath() {
    throw new UnsupportedOperationException();
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#getURL()
   */
  @Override
  public URL getURL() throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#getContents()
   */
  @Override
  public String getContents() throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#apply(de.matrixweb.smaller.resource.vfs.VFS,
   *      de.matrixweb.smaller.resource.Processor, java.util.Map)
   */
  @Override
  public Resource apply(final VFS vfs, final Processor processor,
      final Map<String, String> options) throws IOException {
    // Version 1.1.0 handling
    if (this.resources.isEmpty()) {
      return processor.execute(vfs, null, options);
    }
    if (processor instanceof MultiResourceProcessor) {
      final Resource result = processor.execute(vfs, this, options);
      this.resources.clear();
      this.resources.add(result);
      return result;
    }
    final List<Resource> list = new ArrayList<Resource>();
    for (final Resource resource : this.resources) {
      list.add(resource.apply(vfs, processor, options));
    }
    this.resources.clear();
    this.resources.addAll(list);
    return this;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.resources.toString();
  }

}
