package de.matrixweb.smaller.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.common.Task;
import de.matrixweb.smaller.common.Task.GlobalOptions;
import de.matrixweb.smaller.resource.MultiResource;
import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.ProcessorFactory;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.ResourceResolver;
import de.matrixweb.smaller.resource.Resources;
import de.matrixweb.smaller.resource.SourceMerger;
import de.matrixweb.smaller.resource.Type;

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
   * @param resolver
   *          {@link ResourceResolver} used to locate resources
   * @param task
   *          The task definition
   * @return Returns the processed results as {@link Resource}s
   */
  public Result execute(final ResourceResolver resolver, final Task task) {
    try {
      return execute(createResourceGroup(resolver, task), task);
    } catch (final IOException e) {
      throw new SmallerException("Failed to run processor chain", e);
    }
  }

  private Resources createResourceGroup(final ResourceResolver resolver,
      final Task task) throws IOException {
    final List<String> files = new ArrayList<String>();
    files.addAll(Arrays.asList(task.getIn()));
    final SourceMerger merger = new SourceMerger(
        GlobalOptions.isSourceOnce(task));
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

  /**
   * @param resources
   * @param task
   * @return Returns the processed results as {@link Resource}s
   */
  public Result execute(final Resources resources, final Task task) {
    try {
      String processors = task.getProcessor();
      LOGGER.info("Building processor chain: {}", processors);
      validate(task);
      if (processors.indexOf("merge") == -1) {
        processors = "merge," + processors;
      }
      for (final String name : processors.split(",")) {
        final Processor processor = this.processorFactory.getProcessor(name);
        if (processor != null) {
          for (final Type type : Type.values()) {
            final List<Resource> res = resources.getByType(type);
            if (res.size() > 0 && processor.supportsType(type)) {
              LOGGER.info("Executing processor {} for type ", name, type);
              final List<Resource> results = new ArrayList<Resource>();
              for (final Resource r : res) {
                results.add(r.apply(processor, task.getOptionsFor(name)));
              }
              resources.replace(res, results);
            }
          }
        }
      }

      return new Result(resources);
    } catch (final IOException e) {
      throw new SmallerException("Failed to run processor chain", e);
    }
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

}
