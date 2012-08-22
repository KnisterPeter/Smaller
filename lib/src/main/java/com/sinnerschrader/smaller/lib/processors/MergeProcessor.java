package com.sinnerschrader.smaller.lib.processors;

import java.io.IOException;

import com.sinnerschrader.smaller.resource.MergingProcessor;
import com.sinnerschrader.smaller.resource.MultiResource;
import com.sinnerschrader.smaller.resource.Resource;
import com.sinnerschrader.smaller.resource.SourceMerger;
import com.sinnerschrader.smaller.resource.StringResource;
import com.sinnerschrader.smaller.resource.Type;

/**
 * @author marwol
 */
public class MergeProcessor implements MergingProcessor {

  /**
   * @see com.sinnerschrader.smaller.resource.Processor#supportsType(com.sinnerschrader.smaller.resource.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return true;
  }

  /**
   * @see com.sinnerschrader.smaller.resource.Processor#execute(com.sinnerschrader.smaller.resource.Resource)
   */
  @Override
  public Resource execute(final Resource resource) throws IOException {
    if (resource instanceof MultiResource) {
      return new StringResource(resource.getResolver(), resource.getType(),
          resource.getPath(),
          new SourceMerger().merge(((MultiResource) resource).getResources()));
    }
    return resource;
  }

}
