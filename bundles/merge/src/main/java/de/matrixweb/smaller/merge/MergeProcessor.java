package de.matrixweb.smaller.merge;

import java.io.IOException;
import java.util.Map;

import de.matrixweb.smaller.resource.MergingProcessor;
import de.matrixweb.smaller.resource.MultiResource;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.StringResource;
import de.matrixweb.smaller.resource.Type;
import de.matrixweb.smaller.resource.vfs.VFS;

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
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.smaller.resource.vfs.VFS,
   *      de.matrixweb.smaller.resource.Resource, java.util.Map)
   */
  @Override
  public Resource execute(final VFS vfs, final Resource resource,
      final Map<String, String> options) throws IOException {
    final String typeOption = options.get("type");
    if (!(resource instanceof MultiResource) || typeOption != null
        && resource.getType() != Type.valueOf(typeOption)) {
      return resource;
    }
    return new StringResource(resource.getResolver(), resource.getType(),
        resource.getPath(), resource.getContents());
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#dispose()
   */
  @Override
  public void dispose() {
  }

}
