package de.matrixweb.smaller.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.common.Task;
import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.ProcessorFactory;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.ResourceResolver;
import de.matrixweb.smaller.resource.ResourceUtil;
import de.matrixweb.smaller.resource.Resources;
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
      return execute(ResourceUtil.createResourceGroup(resolver, task), task);
    } catch (final IOException e) {
      throw new SmallerException("Failed to run processor chain", e);
    }
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

      LOGGER.info("Finished executing pipeline");
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
