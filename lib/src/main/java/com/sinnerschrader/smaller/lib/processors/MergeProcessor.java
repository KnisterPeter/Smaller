package com.sinnerschrader.smaller.lib.processors;

import java.io.IOException;

import com.sinnerschrader.smaller.lib.ProcessorChain.Type;
import com.sinnerschrader.smaller.lib.RequestContext;
import com.sinnerschrader.smaller.lib.SourceMerger;
import com.sinnerschrader.smaller.lib.resource.MultiResource;
import com.sinnerschrader.smaller.lib.resource.Resource;
import com.sinnerschrader.smaller.lib.resource.StringResource;

/**
 * @author marwol
 */
public class MergeProcessor implements Processor {

  /**
   * @see com.sinnerschrader.smaller.lib.processors.Processor#supportsType(com.sinnerschrader.smaller.lib.ProcessorChain.Type)
   */
  @Override
  public boolean supportsType(Type type) {
    return true;
  }

  /**
   * @see com.sinnerschrader.smaller.lib.processors.Processor#execute(com.sinnerschrader.smaller.lib.RequestContext,
   *      com.sinnerschrader.smaller.lib.resource.Resource)
   */
  @Override
  public Resource execute(RequestContext context, Resource resource) throws IOException {
    if (resource instanceof MultiResource) {
      return new StringResource(resource.getType(), new SourceMerger().merge(((MultiResource) resource).getResources()));
    }
    return resource;
  }

}
