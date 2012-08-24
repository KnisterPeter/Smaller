package de.matrixweb.smaller.merge;

import java.io.IOException;

import de.matrixweb.smaller.resource.MergingProcessor;
import de.matrixweb.smaller.resource.MultiResource;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.SourceMerger;
import de.matrixweb.smaller.resource.StringResource;
import de.matrixweb.smaller.resource.Type;

/**
 * @author marwol
 */
public class MergeProcessor implements MergingProcessor {

  /**
   * @see de.matrixweb.smaller.resource.Processor#supportsType(de.matrixweb.smaller.resource.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return true;
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.smaller.resource.Resource)
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
