package de.matrixweb.smaller.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import de.matrixweb.smaller.common.Task;
import de.matrixweb.smaller.common.Task.GlobalOptions;

/**
 * @author marwol
 */
public class ResourceUtil {

  /**
   * @param name
   * @return
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
   * @param resolver
   * @param task
   * @return
   * @throws IOException
   */
  public static Resources createResourceGroup(final ResourceResolver resolver,
      final Task task) throws IOException {
    final List<String> files = new ArrayList<String>();
    files.addAll(Arrays.asList(task.getIn()));
    final SourceMerger merger = new SourceMerger(
        GlobalOptions.isSourceOnce(task));
    return createResourceGroupImpl(resolver, merger, files);
  }

  /**
   * @param resolver
   * @param files
   * @return
   * @throws IOException
   */
  public static Resources createResourceGroup(final ResourceResolver resolver,
      final List<String> files) throws IOException {
    return createResourceGroupImpl(resolver, new SourceMerger(), files);
  }

  private static Resources createResourceGroupImpl(
      final ResourceResolver resolver, final SourceMerger merger,
      final List<String> files) throws IOException {
    final Resources resources = new Resources(merger.getResources(resolver,
        files));
    List<Resource> res = resources.getByType(Type.JS);
    if (res.size() > 1) {
      resources.replace(res, new MultiResource(merger, resolver, res.get(0)
          .getPath(), res));
    }
    res = resources.getByType(Type.CSS);
    if (res.size() > 1) {
      resources.replace(res, new MultiResource(merger, resolver, res.get(0)
          .getPath(), res));
    }
    return resources;
  }

}
