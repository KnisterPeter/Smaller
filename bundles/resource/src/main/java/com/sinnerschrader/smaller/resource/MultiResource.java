package com.sinnerschrader.smaller.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
   * @see com.sinnerschrader.smaller.resource.Resource#getResolver()
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
   * @see com.sinnerschrader.smaller.resource.Resource#getType()
   */
  @Override
  public Type getType() {
    return this.resources.isEmpty() ? Type.UNKNOWN : this.resources.get(0)
        .getType();
  }

  /**
   * @see com.sinnerschrader.smaller.resource.Resource#getPath()
   */
  @Override
  public String getPath() {
    return this.path;
  }

  /**
   * @see com.sinnerschrader.smaller.resource.Resource#getContents()
   */
  @Override
  public String getContents() throws IOException {
    return new SourceMerger().merge(this.resources);
  }

  /**
   * @see com.sinnerschrader.smaller.resource.Resource#apply(com.sinnerschrader.smaller.resource.Processor)
   */
  @Override
  public Resource apply(final Processor processor) throws IOException {
    if (processor.canMerge()) {
      return processor.execute(this);
    }
    final List<Resource> list = new ArrayList<Resource>();
    for (final Resource resource : this.resources) {
      list.add(resource.apply(processor));
    }
    this.resources.clear();
    this.resources.addAll(list);
    return this;
  }

}
