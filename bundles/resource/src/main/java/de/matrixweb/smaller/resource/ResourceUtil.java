package de.matrixweb.smaller.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import de.matrixweb.smaller.common.Task;
import de.matrixweb.smaller.common.Task.GlobalOptions;
import de.matrixweb.smaller.common.Version;
import de.matrixweb.smaller.resource.vfs.VFS;
import de.matrixweb.smaller.resource.vfs.VFile;

/**
 * @author marwol
 */
public class ResourceUtil {

  /**
   * @param name
   * @return Returns the type of the given file name
   */
  public static Type getType(final String name) {
    final String ext = FilenameUtils.getExtension(name);
    for (final Type type : Type.values()) {
      if (type.isOfType(ext)) {
        return type;
      }
    }
    return Type.UNKNOWN;
  }

  /**
   * @param version
   *          The spec version to execute
   * @param resolver
   *          The {@link ResourceResolver} to use for resource resolving
   * @param task
   *          The input {@link Task}
   * @return Returns the resolved input resources
   * @throws IOException
   */
  public static Resources createResourceGroup(final Version version,
      final ResourceResolver resolver, final Task task) throws IOException {
    if (version.isAtLeast(Version._1_0_0)) {
      // Since version 1.1.0 no multi-resources
      return createResourceGroupImpl0(resolver,
          new SourceMerger(GlobalOptions.isSourceOnce(task)),
          new ArrayList<String>(Arrays.asList(task.getIn())));
    }
    return createResourceGroup(resolver, task);
  }

  /**
   * @param resolver
   * @param task
   * @return Returns a list of input resources resolved from the given
   *         {@link Task}
   * @throws IOException
   */
  public static Resources createResourceGroup(final ResourceResolver resolver,
      final Task task) throws IOException {
    return createResourceGroupImpl1(resolver,
        new SourceMerger(GlobalOptions.isSourceOnce(task)),
        new ArrayList<String>(Arrays.asList(task.getIn())));
  }

  private static Resources createResourceGroupImpl0(
      final ResourceResolver resolver, final SourceMerger merger,
      final List<String> files) throws IOException {
    return new Resources(merger.getResources(resolver, files));
  }

  private static Resources createResourceGroupImpl1(
      final ResourceResolver resolver, final SourceMerger merger,
      final List<String> files) throws IOException {
    final Resources resources = createResourceGroupImpl0(resolver, merger,
        files);
    List<Resource> res = resources.getByType(Type.JS);
    if (res.size() > 1) {
      resources.replace(res, new MultiResource(merger, resolver, res));
    }
    res = resources.getByType(Type.CSS);
    if (res.size() > 1) {
      resources.replace(res, new MultiResource(merger, resolver, res));
    }
    return resources;
  }

  /**
   * @param vfs
   * @param resolver
   * @param ext
   * @return
   * @throws IOException
   */
  public static List<VFile> getFilesByExtension(final VFS vfs, final String ext)
      throws IOException {
    return getFilesByExtension(vfs.find("/"), ext);
  }

  private static List<VFile> getFilesByExtension(final VFile file,
      final String ext) throws IOException {
    final List<VFile> files = new ArrayList<VFile>();
    if (file.isDirectory()) {
      for (final VFile child : file.getChildren()) {
        files.addAll(getFilesByExtension(child, ext));
      }
    } else if (ext.equals(FilenameUtils.getExtension(file.getName()))) {
      files.add(file);
    }
    return files;
  }

}
