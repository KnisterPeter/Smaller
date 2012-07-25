package com.sinnerschrader.smaller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.sinnerschrader.smaller.common.Manifest;
import com.sinnerschrader.smaller.common.Manifest.Task;
import com.sinnerschrader.smaller.common.SmallerException;
import com.sinnerschrader.smaller.processors.Processor;

/**
 * @author marwol
 */
public class ProcessorChain {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorChain.class);

  /**
   * @param context
   * @throws IOException
   */
  public void execute(final RequestContext context) throws IOException {
    final Manifest manifest = context.getManifest();
    final Task task = manifest.getNext();

    String jsSource = this.getMergedSourceFiles(context, task, Type.JS);
    String cssSource = this.getMergedSourceFiles(context, task, Type.CSS);

    LOGGER.info("Building processor chain: {}", task.getProcessor());
    this.validate(context, task);
    for (final String name : task.getProcessor().split(",")) {
      final Processor processor = this.createProcessor(name);
      if (processor != null) {
        LOGGER.info("Executing processor {}", name);
        if (processor.supportsType(Type.JS)) {
          jsSource = processor.execute(context, jsSource);
        }
        if (processor.supportsType(Type.CSS)) {
          cssSource = processor.execute(context, cssSource);
        }
      }
    }

    this.writeResult(context, task, jsSource, Type.JS);
    this.writeResult(context, task, cssSource, Type.CSS);
  }

  private boolean validate(final RequestContext context, final Task task) {
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

  private void writeResult(final RequestContext context, final Task task, final String source, final Type type) throws IOException {
    final String jsOutputFile = this.getTargetFile(context.getOutput(), task.getOut(), type);
    if (jsOutputFile != null) {
      FileUtils.writeStringToFile(new File(jsOutputFile), source);
    }
  }

  private String getMergedSourceFiles(final RequestContext context, final Task task, final Type type) throws IOException {
    final List<String> sourceFiles = this.getSourceFiles(context.getInput(), task.getIn(), type);
    return this.merge(sourceFiles, "\n");
  }

  private List<String> getSourceFiles(final File base, final String[] in, final Type type) throws IOException {
    final List<String> inputs = Lists.newArrayList();
    for (final String s : in) {
      final String ext = FilenameUtils.getExtension(s);
      switch (type) {
      case JS:
        if (this.isJsSourceFile(ext)) {
          inputs.add(new File(base, s).getAbsolutePath());
        } else if (ext.equals("json")) {
          inputs.addAll(this.getJsonSourceFiles(base, s));
        }
        break;
      case CSS:
        if (this.isCssSourceFile(ext)) {
          inputs.add(new File(base, s).getAbsolutePath());
        }
        break;
      }
    }
    return inputs;
  }

  private List<String> getJsonSourceFiles(final File base, final String filename) throws IOException {
    final List<String> list = Lists.newArrayList();
    for (final String s : new ObjectMapper().readValue(FileUtils.readFileToString(new File(base, filename)), String[].class)) {
      list.add(new File(base, s).getAbsolutePath());
    }
    return list;
  }

  private boolean isJsSourceFile(final String ext) {
    return ext.equals("js") || ext.equals("coffee");
  }

  private boolean isCssSourceFile(final String ext) {
    return ext.equals("css") || ext.equals("less") || ext.equals("sass");
  }

  private String getTargetFile(final File base, final String[] out, final Type type) {
    String target = null;
    for (final String s : out) {
      final String ext = FilenameUtils.getExtension(s);
      switch (type) {
      case JS:
        if (ext.equals("js")) {
          target = new File(base, s).getAbsolutePath();
        }
        break;
      case CSS:
        if (ext.equals("css")) {
          target = new File(base, s).getAbsolutePath();
        }
        break;
      }
    }
    return target;
  }

  private Processor createProcessor(final String name) {
    try {
      return (Processor) Class.forName("com.sinnerschrader.smaller.processors." + StringUtils.capitalize(name.toLowerCase()) + "Processor").newInstance();
    } catch (final InstantiationException e) {
      LOGGER.warn("Ignoring invalid processor " + name, e);
    } catch (final IllegalAccessException e) {
      LOGGER.warn("Ignoring invalid processor " + name, e);
    } catch (final ClassNotFoundException e) {
      LOGGER.warn("Ignoring invalid processor " + name, e);
    }
    return null;
  }

  private String merge(final List<String> paths, final String separator) throws IOException {
    final List<String> contents = Lists.newArrayList();
    for (final String path : paths) {
      contents.add(FileUtils.readFileToString(new File(path)));
    }
    return Joiner.on(separator).join(contents);
  }

  /** */
  public enum Type {
    /** */
    JS,
    /** */
    CSS
  }

}
