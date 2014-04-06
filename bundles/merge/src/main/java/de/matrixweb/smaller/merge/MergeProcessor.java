package de.matrixweb.smaller.merge;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.common.Version;
import de.matrixweb.smaller.resource.MergingProcessor;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.ResourceGroup;
import de.matrixweb.smaller.resource.SourceMerger;
import de.matrixweb.smaller.resource.Type;
import de.matrixweb.vfs.VFS;
import de.matrixweb.vfs.VFSUtils;
import de.matrixweb.vfs.VFile;

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
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.vfs.VFS,
   *      de.matrixweb.smaller.resource.Resource, java.util.Map)
   */
  @Override
  public Resource execute(final VFS vfs, final Resource resource,
      final Map<String, Object> options) throws IOException {
    // Version 1.0.0 handling
    if (getVersion(options).isAtLeast(Version._1_0_0)) {
      try {
        if (!(resource instanceof ResourceGroup) && resource != null
            && FilenameUtils.isExtension(resource.getPath(), "json")) {
          return executeSimpleMerge(vfs, resource, options);
        }
        return executeComplexMerge(vfs, resource, options);
      } catch (final IOException e) {
        throw new SmallerException("Failed to merge files", e);
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

  private Version getVersion(final Map<String, Object> options) {
    final Object value = options.get("version");
    return value == null ? Version.UNDEFINED : Version.getVersion(value
        .toString());
  }

  private Resource executeSimpleMerge(final VFS vfs, final Resource resource,
      final Map<String, Object> options) throws IOException {
    return resource.getResolver()
        .resolve(
            new SourceMerger("once".equals(options.get("source")) ? true
                : false).getMergedJsonFile(vfs, resource.getResolver(),
                resource.getPath()));
  }

  private Resource executeComplexMerge(final VFS vfs, final Resource resource,
      final Map<String, Object> options) throws IOException {
    final Object typeOption = options.get("type");
    if (!(resource instanceof ResourceGroup) || typeOption != null
        && resource.getType() != Type.valueOf(typeOption.toString())) {
      return resource;
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

  /**
   * @see de.matrixweb.smaller.resource.Processor#dispose()
   */
  @Override
  public void dispose() {
  }

}
