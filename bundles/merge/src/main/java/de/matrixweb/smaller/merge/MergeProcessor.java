package de.matrixweb.smaller.merge;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import de.matrixweb.smaller.common.Version;
import de.matrixweb.smaller.resource.MergingProcessor;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.ResourceGroup;
import de.matrixweb.smaller.resource.Type;
import de.matrixweb.smaller.resource.vfs.VFS;
import de.matrixweb.smaller.resource.vfs.VFSUtils;
import de.matrixweb.smaller.resource.vfs.VFile;

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
    // Version 1.0.0 handling
    if (Version.getVersion(options.get("version")).isAtLeast(Version._1_0_0)) {
      final String typeOption = options.get("type");
      if (!(resource instanceof ResourceGroup) || typeOption != null
          && resource.getType() != Type.valueOf(typeOption)) {
        return resource;
      }

      if (!(resource instanceof ResourceGroup)) {
        throw new IllegalArgumentException();
      }
      final ResourceGroup group = (ResourceGroup) resource;
      final Resource input = group.getResources().get(0);

      final VFile snapshot = vfs.stack();
      try {
        final VFile target = vfs.find(input.getPath());
        final Writer writer = VFSUtils.createWriter(target);
        try {
          writer.write(group.getMerger().merge(group.getResources()));
        } finally {
          IOUtils.closeQuietly(writer);
        }
        return input.getResolver().resolve(target.getPath());
      } catch (final IOException e) {
        vfs.rollback(snapshot);
        throw e;
      }
    }

    final VFile snapshot = vfs.stack();
    try {
      final VFile file = vfs.find(resource.getPath());
      VFSUtils.write(file, resource.getContents());
      return resource.getResolver().resolve(file.getPath());
    } catch (final IOException e) {
      vfs.rollback(snapshot);
      throw e;
    }
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#dispose()
   */
  @Override
  public void dispose() {
  }

}
