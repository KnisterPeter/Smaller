package de.matrixweb.smaller.resource;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.matrixweb.vfs.VFS;

/**
 * @author marwol
 * @deprecated Without replacement
 */
@Deprecated
public class MultiResource implements Resource {

  private final SourceMerger merger;

  private final ResourceResolver resolver;

  private final List<Resource> resources;

  /**
   * @param merger
   * @param resolver
   * @param resources
   */
  public MultiResource(final SourceMerger merger,
      final ResourceResolver resolver, final List<Resource> resources) {
    this.merger = merger;
    this.resolver = resolver;
    this.resources = resources;
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#getResolver()
   */
  @Override
  public ResourceResolver getResolver() {
    return this.resolver;
  }

  /**
   * @return the resources
   */
  public List<Resource> getResources() {
    return this.resources;
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#getType()
   */
  @Override
  public Type getType() {
    return this.resources.isEmpty() ? Type.UNKNOWN : this.resources.get(0)
        .getType();
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#getPath()
   */
  @Override
  public String getPath() {
    return this.resources.isEmpty() ? "" : this.resources.get(0).getPath();
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#getURL()
   */
  @Override
  public URL getURL() {
    return null;
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#getContents()
   */
  @Override
  public String getContents() throws IOException {
    return this.merger.merge(this.resources);
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#apply(de.matrixweb.vfs.VFS,
   *      de.matrixweb.smaller.resource.Processor, java.util.Map)
   */
  @Override
  public Resource apply(final VFS vfs, final Processor processor,
      final Map<String, Object> options) throws IOException {
    if (processor instanceof MultiResourceProcessor) {
      return processor.execute(vfs, this, options);
    }
    final List<Resource> list = new ArrayList<Resource>();
    for (final Resource resource : this.resources) {
      list.add(resource.apply(vfs, processor, options));
    }
    this.resources.clear();
    this.resources.addAll(list);
    return this;
  }

}
