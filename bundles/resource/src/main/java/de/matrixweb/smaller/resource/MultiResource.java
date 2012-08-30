package de.matrixweb.smaller.resource;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author marwol
 */
public class MultiResource implements Resource {

  private final ResourceResolver resolver;

  private final String path;

  private final List<Resource> resources;

  /**
   * @param resolver
   * @param path
   * @param resources
   */
  public MultiResource(final ResourceResolver resolver, final String path,
      final List<Resource> resources) {
    this.resolver = resolver;
    this.path = path;
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
    return this.path;
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
    return new SourceMerger().merge(this.resources);
  }

  /**
   * 
   * @see de.matrixweb.smaller.resource.Resource#apply(de.matrixweb.smaller.resource.Processor,
   *      java.util.Map)
   */
  @Override
  public Resource apply(final Processor processor,
      final Map<String, String> options) throws IOException {
    if (processor instanceof MergingProcessor) {
      return processor.execute(this, options);
    }
    final List<Resource> list = new ArrayList<Resource>();
    for (final Resource resource : this.resources) {
      list.add(resource.apply(processor, options));
    }
    this.resources.clear();
    this.resources.addAll(list);
    return this;
  }

}
