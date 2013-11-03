package de.matrixweb.smaller.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.common.Task;
import de.matrixweb.smaller.common.Task.GlobalOptions;
import de.matrixweb.smaller.common.Version;
import de.matrixweb.smaller.resource.MergingProcessor;
import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.ProcessorFactory;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.ResourceGroup;
import de.matrixweb.smaller.resource.ResourceResolver;
import de.matrixweb.smaller.resource.ResourceUtil;
import de.matrixweb.smaller.resource.Resources;
import de.matrixweb.smaller.resource.SourceMerger;
import de.matrixweb.smaller.resource.Type;
import de.matrixweb.vfs.VFS;
import de.matrixweb.vfs.VFSUtils;
import de.matrixweb.vfs.VFile;

/**
 * @author marwol
 */
public class Pipeline {

  private static final Logger LOGGER = LoggerFactory.getLogger(Pipeline.class);

  private final ProcessorFactory processorFactory;

  /**
   * @param processorFactory
   */
  public Pipeline(final ProcessorFactory processorFactory) {
    this.processorFactory = processorFactory;
  }

  /**
   * @param version
   *          The spec version to execute
   * @param vfs
   *          The file system to operate in
   * @param resolver
   *          {@link ResourceResolver} used to locate resources
   * @param task
   *          The task definition
   * @return Returns the processed results as {@link Resource}s
   */
  public Result execute(final Version version, final VFS vfs,
      final ResourceResolver resolver, final Task task) {
    try {
      return execute(version, vfs, resolver,
          ResourceUtil.createResourceGroup(version, resolver, task), task);
    } catch (final IOException e) {
      throw new SmallerException("Failed to run processor chain", e);
    }
  }

  private Result execute(final Version version, final VFS vfs,
      final ResourceResolver resolver, final Resources resources,
      final Task task) {
    try {
      validate(task);

      final List<ProcessorOptions> entries = setupProcessors(version, task);
      if (version.isAtLeast(Version._1_0_0)) {
        execute1_0(vfs, resolver, resources, entries, task);
      } else {
        execute0_0(vfs, resources, entries);
      }

      LOGGER.info("Finished executing pipeline");
      return prepareResult(vfs, resolver, task);
    } catch (final IOException e) {
      throw new SmallerException("Failed to run processor chain", e);
    }
  }

  private void execute0_0(final VFS vfs, final Resources resources,
      final List<ProcessorOptions> entries) throws IOException {
    for (final ProcessorOptions entry : entries) {
      for (final Type type : Type.values()) {
        final List<Resource> res = resources.getByType(type);
        if (res.size() > 0 && entry.processor.supportsType(type)) {
          LOGGER.info("Executing processor {} for type {}", entry.name, type);
          final List<Resource> results = new ArrayList<Resource>();
          for (final Resource r : res) {
            results.add(r.apply(vfs, entry.processor, entry.options));
          }
          resources.replace(res, results);
        }
      }
    }
  }

  private void execute1_0(final VFS vfs, final ResourceResolver resolver,
      final Resources resources, final List<ProcessorOptions> entries,
      final Task task) throws IOException {
    for (final ProcessorOptions entry : entries) {
      for (final Type type : Type.values()) {
        final List<Resource> res = resources.getByType(type);
        if (entry.processor.supportsType(type)) {
          LOGGER.info("Executing processor {} for type {}", entry.name, type);
          final List<Resource> results = new ArrayList<Resource>();
          // TODO: SourceMerger should not be required here
          final ResourceGroup group = new ResourceGroup(res, new SourceMerger(
              GlobalOptions.isSourceOnce(task)));
          group.apply(vfs, entry.processor, entry.options);
          results.addAll(group.getResources());
          resources.replace(res, results);
        }
      }
    }
  }

  private List<ProcessorOptions> setupProcessors(final Version version,
      final Task task) {
    final List<ProcessorOptions> list = new ArrayList<Pipeline.ProcessorOptions>();

    boolean hasJsMerger = false;
    boolean hasCssMerger = false;
    final String processors = task.getProcessor();
    LOGGER.info("Building processor chain: {}", processors);
    for (final String name : processors.split(",")) {
      final Processor processor = this.processorFactory.getProcessor(name);
      if (processor != null) {
        hasJsMerger |= processor instanceof MergingProcessor
            && processor.supportsType(Type.JS);
        hasCssMerger |= processor instanceof MergingProcessor
            && processor.supportsType(Type.CSS);
        list.add(new ProcessorOptions(name, processor, addVersionToOptions(
            task.getOptionsFor(name), version)));
      }
    }
    // Since version 1.1.0 no implicit merger
    if (version == Version.UNDEFINED) {
      if (!hasJsMerger) {
        list.add(0, createTypeMerger(Type.JS));
      }
      if (!hasCssMerger) {
        list.add(0, createTypeMerger(Type.CSS));
      }
    }

    return list;
  }

  private Map<String, String> addVersionToOptions(
      final Map<String, String> options, final Version version) {
    options.put("version", version.toString());
    return options;
  }

  private ProcessorOptions createTypeMerger(final Type type) {
    final Map<String, String> options = new HashMap<String, String>();
    options.put("type", type.name());
    return new ProcessorOptions("merge",
        this.processorFactory.getProcessor("merge"), options);
  }

  private boolean validate(final Task task) {
    if (CollectionUtils.exists(
        CollectionUtils.getCardinalityMap(
            CollectionUtils.collect(Arrays.asList(task.getOut()),
                new Transformer() {
                  @Override
                  public Object transform(final Object input) {
                    return FilenameUtils.getExtension(input.toString());
                  }
                })).values(), new Predicate() {
          @Override
          public boolean evaluate(final Object object) {
            return ((Integer) object).intValue() > 1;
          }
        })) {
      throw new SmallerException("Each output type must exist only once");
    }

    final String[] processors = task.getProcessor().toLowerCase().split(",");
    boolean cssembedFound = false;
    for (final String processor : processors) {
      if (processor.equals("cssembed")) {
        cssembedFound = true;
      } else if (processor.equals("yuicompressor") && cssembedFound) {
        throw new SmallerException("yuiCompressor must run before cssembed");
      }
    }

    return true;
  }

  private Result prepareResult(final VFS vfs, final ResourceResolver resolver,
      final Task task) throws IOException {
    final Resources resources = new Resources();
    for (final String out : task.getOut()) {
      final String ext = FilenameUtils.getExtension(out);
      final VFile file = findLastModified(vfs.find("/"), ext);
      if (file != null) {
        final VFile target = vfs.find('/' + out);
        VFSUtils.copy(file, target);
        resources.addResource(resolver.resolve(target.getPath()));
      }
    }
    return new Result(resources);
  }

  private VFile findLastModified(final VFile file, final String ext)
      throws IOException {
    VFile newest = null;
    if (file.isDirectory()) {
      for (final VFile child : file.getChildren()) {
        final VFile temp = findLastModified(child, ext);
        if (newest == null || temp != null
            && temp.getLastModified() > newest.getLastModified()) {
          newest = temp;
        }
      }
    } else if (ext.equals(FilenameUtils.getExtension(file.getName()))
        && (newest == null || file.getLastModified() > newest.getLastModified())) {
      newest = file;
    }
    return newest;
  }

  private static class ProcessorOptions {

    private final String name;

    private final Processor processor;

    private final Map<String, String> options;

    ProcessorOptions(final String name, final Processor processor,
        final Map<String, String> options) {
      this.name = name;
      this.processor = processor;
      this.options = options;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return this.name;
    }

  }

}
