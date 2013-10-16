package de.matrixweb.smaller.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.common.Task;
import de.matrixweb.smaller.resource.MergingProcessor;
import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.ProcessorFactory;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.ResourceResolver;
import de.matrixweb.smaller.resource.ResourceUtil;
import de.matrixweb.smaller.resource.Resources;
import de.matrixweb.smaller.resource.Type;
import de.matrixweb.smaller.resource.vfs.VFS;

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
   * @param vfs
   *          The file system to operate in
   * @param resolver
   *          {@link ResourceResolver} used to locate resources
   * @param task
   *          The task definition
   * @return Returns the processed results as {@link Resource}s
   */
  public Result execute(final VFS vfs, final ResourceResolver resolver,
      final Task task) {
    try {
      return execute(vfs, ResourceUtil.createResourceGroup(resolver, task),
          task);
    } catch (final IOException e) {
      throw new SmallerException("Failed to run processor chain", e);
    }
  }

  /**
   * @param vfs
   * @param resources
   * @param task
   * @return Returns the processed results as {@link Resource}s
   */
  public Result execute(final VFS vfs, final Resources resources,
      final Task task) {
    try {
      validate(task);

      final List<ProcessorOptions> entries = setupProcessors(task);
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

      LOGGER.info("Finished executing pipeline");
      return new Result(resources);
    } catch (final IOException e) {
      throw new SmallerException("Failed to run processor chain", e);
    }
  }

  private List<ProcessorOptions> setupProcessors(final Task task) {
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
        list.add(new ProcessorOptions(name, processor, task.getOptionsFor(name)));
      }
    }
    if (!hasJsMerger) {
      list.add(0, createTypeMerger(Type.JS));
    }
    if (!hasCssMerger) {
      list.add(0, createTypeMerger(Type.CSS));
    }

    return list;
  }

  private ProcessorOptions createTypeMerger(final Type type) {
    final Map<String, String> options = new HashMap<String, String>();
    options.put("type", type.name());
    return new ProcessorOptions("merge",
        this.processorFactory.getProcessor("merge"), options);
  }

  private boolean validate(final Task task) {
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

  }

}
