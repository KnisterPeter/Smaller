package com.sinnerschrader.smaller.processors;

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
import com.sinnerschrader.smaller.RequestContext;
import com.sinnerschrader.smaller.common.Manifest;
import com.sinnerschrader.smaller.common.Manifest.Task;

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
    final String[] jsSourceFiles = this.getSourceFiles(context.getInput(), task.getIn(), Type.JS);
    final String[] cssSourceFiles = this.getSourceFiles(context.getInput(), task.getIn(), Type.CSS);

    String jsSource = this.merge(jsSourceFiles, "\n");
    String cssSource = this.merge(cssSourceFiles, "\n");

    LOGGER.info("Building processor chain: {}", task.getProcessor());
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

    final String jsOutputFile = this.getTargetFile(context.getOutput(), task.getOut(), Type.JS);
    if (jsOutputFile != null) {
      FileUtils.writeStringToFile(new File(jsOutputFile), jsSource);
    }
    final String cssOutputFile = this.getTargetFile(context.getOutput(), task.getOut(), Type.CSS);
    if (cssOutputFile != null) {
      FileUtils.writeStringToFile(new File(cssOutputFile), cssSource);
    }
  }

  private String[] getSourceFiles(final File base, final String[] in, final Type type) throws IOException {
    final List<String> inputs = Lists.newArrayList();
    for (final String s : in) {
      final String ext = FilenameUtils.getExtension(s);
      switch (type) {
      case JS:
        if (ext.equals("js") || ext.equals("coffee")) {
          inputs.add(new File(base, s).getAbsolutePath());
        } else if (ext.equals("json")) {
          for (final String s1 : new ObjectMapper().readValue(FileUtils.readFileToString(new File(base, s)), String[].class)) {
            inputs.add(new File(base, s1).getAbsolutePath());
          }
        }
        break;
      case CSS:
        if (ext.equals("css") || ext.equals("less") || ext.equals("sass")) {
          inputs.add(new File(base, s).getAbsolutePath());
        }
        break;
      }
    }
    return inputs.toArray(new String[inputs.size()]);
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

  private String merge(final String[] paths, final String separator) throws IOException {
    final List<String> contents = Lists.newArrayList();
    for (final String path : paths) {
      contents.add(FileUtils.readFileToString(new File(path)));
    }
    return Joiner.on(separator).join(contents);
  }

  enum Type {
    /** */
    JS,
    /** */
    CSS
  }

}
