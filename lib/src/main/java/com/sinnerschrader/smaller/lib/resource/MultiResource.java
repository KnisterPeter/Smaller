package com.sinnerschrader.smaller.lib.resource;

import java.io.IOException;
import java.util.List;

import com.sinnerschrader.smaller.lib.SourceMerger;

/**
 * @author marwol
 */
public class MultiResource implements Resource {

  private List<Resource> resources;

  /**
   * @param resources
   */
  public MultiResource(List<Resource> resources) {
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
    return resources.isEmpty() ? Type.UNKNOWN : resources.get(0).getType();
  }

  /**
   * @see com.sinnerschrader.smaller.lib.resource.Resource#getContents()
   */
  @Override
  public String getContents() throws IOException {
    return new SourceMerger().merge(resources);
  }

}
