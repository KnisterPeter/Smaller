package com.sinnerschrader.smaller.lib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinnerschrader.smaller.common.SmallerException;
import com.sinnerschrader.smaller.common.Task;
import com.sinnerschrader.smaller.lib.processors.Processor;
import com.sinnerschrader.smaller.lib.resource.MultiResource;
import com.sinnerschrader.smaller.lib.resource.Resource;
import com.sinnerschrader.smaller.lib.resource.ResourceResolver;

/**
 * @author marwol
 */
public class ProcessorChain {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorChain.class);

  /**
   * @param resolver
   *          {@link ResourceResolver} used to locate resources
   * @param task
   *          The task definition
   * @return Returns the processed results as {@link Resource}s
   * @throws IOException
   */
  public Result execute(final ResourceResolver resolver, final Task task) {
    try {
      Resource jsSource = getMergedSourceFiles(resolver, task, Type.JS);
      Resource cssSource = getMergedSourceFiles(resolver, task, Type.CSS);
      return execute(jsSource, cssSource, task);
    } catch (final IOException e) {
      throw new SmallerException("Failed to run processor chain", e);
    }
  }

  private Resource getMergedSourceFiles(final ResourceResolver resolver, final Task task, final Type type) throws IOException {
    String multipath = null;
    List<String> files = new ArrayList<String>();
    if (type == Type.JS) {
      files.addAll(Arrays.asList(task.getIn("js", "coffee", "json")));
    } else if (type == Type.CSS) {
      files.addAll(Arrays.asList(task.getIn("css", "less", "sass")));
    }
    if (files.size() > 0) {
      multipath = files.get(0);
    }
    return new MultiResource(resolver, resolver.resolve(multipath).getPath(), new SourceMerger().getResources(resolver, files));
  }

  /**
   * @param jsSource
   * @param cssSource
   * @param task
   * @return Returns the processed results as {@link Resource}s
   */
  public Result execute(final Resource jsSource, final Resource cssSource, final Task task) {
    Resource js = jsSource;
    Resource css = cssSource;
    try {
      String processors = task.getProcessor();
      LOGGER.info("Building processor chain: {}", processors);
      validate(task);
      if (processors.indexOf("merge") == -1) {
        processors = "merge," + processors;
      }
      for (final String name : processors.split(",")) {
        final Processor processor = createProcessor(name);
        if (processor != null) {
          LOGGER.info("Executing processor {}", name);
          if (processor.supportsType(Type.JS)) {
            js = js.apply(processor);
          }
          if (processor.supportsType(Type.CSS)) {
            css = css.apply(processor);
          }
        }
      }

      return new Result(js, css);
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

  private Processor createProcessor(final String name) {
    try {
      return (Processor) Class.forName("com.sinnerschrader.smaller.lib.processors." + StringUtils.capitalize(name.toLowerCase()) + "Processor").newInstance();
    } catch (final InstantiationException e) {
      LOGGER.warn("Ignoring invalid processor " + name, e);
    } catch (final IllegalAccessException e) {
      LOGGER.warn("Ignoring invalid processor " + name, e);
    } catch (final ClassNotFoundException e) {
      LOGGER.warn("Ignoring invalid processor " + name, e);
    }
    return null;
  }

  /** */
  public enum Type {
    /** */
    JS,
    /** */
    CSS
  }

}
