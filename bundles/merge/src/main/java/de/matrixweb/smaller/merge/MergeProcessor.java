package de.matrixweb.smaller.merge;

import java.io.IOException;
import java.util.Map;

import de.matrixweb.smaller.resource.MergingProcessor;
import de.matrixweb.smaller.resource.MultiResource;
import de.matrixweb.smaller.resource.Resource;
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
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.smaller.resource.Resource,
   *      java.util.Map)
   */
  @Override
  public Resource execute(final Resource resource,
      final Map<String, String> options) throws IOException {
    if (resource instanceof MultiResource) {
      return new StringResource(resource.getResolver(), resource.getType(),
          resource.getPath(), resource.getContents());
    }
    return resource;
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#dispose()
   */
  @Override
  public void dispose() {
  }

}
