package com.sinnerschrader.smaller.lib.resource;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.sinnerschrader.smaller.lib.SourceMerger;
import com.sinnerschrader.smaller.lib.processors.MergeProcessor;
import com.sinnerschrader.smaller.lib.processors.Processor;

/**
 * @author marwol
 */
public class MultiResource implements Resource {

  private final String path;

  private final List<Resource> resources;

  /**
   * @param path
   * @param resources
   */
  public MultiResource(final String path, final List<Resource> resources) {
    this.path = path;
    this.resources = resources;
  }

  /**
   * @return the resources
   */
  public List<Resource> getResources() {
    return this.resources;
  }

  /**
   * @see com.sinnerschrader.smaller.lib.resource.Resource#getType()
   */
  @Override
  public Type getType() {
    return this.resources.isEmpty() ? Type.UNKNOWN : this.resources.get(0).getType();
  }

  /**
   * @see com.sinnerschrader.smaller.lib.resource.Resource#getPath()
   */
  @Override
  public String getPath() {
    return this.path;
  }

  /**
   * @see com.sinnerschrader.smaller.lib.resource.Resource#getContents()
   */
  @Override
  public String getContents() throws IOException {
    return new SourceMerger().merge(this.resources);
  }

  /**
   * @see com.sinnerschrader.smaller.lib.resource.Resource#apply(com.sinnerschrader.smaller.lib.processors.Processor)
   */
  @Override
  public Resource apply(final Processor processor) throws IOException {
    if (processor instanceof MergeProcessor) {
      return processor.execute(this);
    }
    List<Resource> list = Lists.newArrayList();
    for (Resource resource : this.resources) {
      list.add(resource.apply(processor));
    }
    this.resources.clear();
    this.resources.addAll(list);
    return this;
  }

}
