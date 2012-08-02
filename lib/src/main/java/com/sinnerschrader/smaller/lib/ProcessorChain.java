package com.sinnerschrader.smaller.lib;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.sinnerschrader.smaller.common.Manifest;
import com.sinnerschrader.smaller.common.Manifest.Task;
import com.sinnerschrader.smaller.common.SmallerException;
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
   * @param context
   * @throws IOException
   */
  public void execute(final RequestContext context) {
    try {
      final Manifest manifest = context.getManifest();
      final Task task = manifest.getNext();

      Resource jsSource = this.getMergedSourceFiles(context, task, Type.JS);
      Resource cssSource = this.getMergedSourceFiles(context, task, Type.CSS);

      String processors = task.getProcessor();
      LOGGER.info("Building processor chain: {}", processors);
      this.validate(context, task);
      if (processors.indexOf("merge") == -1) {
        processors = "merge," + processors;
      }
      for (final String name : processors.split(",")) {
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
    } catch (final IOException e) {
      throw new SmallerException("Failed to run processor chain", e);
    }
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

  private void writeResult(final RequestContext context, final Task task, final Resource resource, final Type type) throws IOException {
    final String jsOutputFile = this.getTargetFile(context.getOutput(), task.getOut(), type);
    if (jsOutputFile != null) {
      FileUtils.writeStringToFile(new File(jsOutputFile), resource.getContents());
    }
  }

  private Resource getMergedSourceFiles(final RequestContext context, final Task task, final Type type) throws IOException {
    List<String> files = Lists.newArrayList();
    for (String in : task.getIn()) {
      String path = new File(context.getInput(), in).getAbsolutePath();
      String ext = FilenameUtils.getExtension(path);
      if (type == Type.JS && (isJsSourceFile(ext) || ext.equals("json"))) {
        files.add(path);
      } else if (type == Type.CSS && isCssSourceFile(ext)) {
        files.add(path);
      }
    }
    return new MultiResource(new SourceMerger().getResources(new BaseFileResourceResolver(context.getInput()), files));
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

  private static class BaseFileResourceResolver implements ResourceResolver {

    private File base;

    /**
     * @param base
     */
    public BaseFileResourceResolver(File base) {
      this.base = base;
    }

    /**
     * 
     */
    @Override
    public Resource resolve(final String path) {
      return new Resource() {
        @Override
        public com.sinnerschrader.smaller.lib.resource.Type getType() {
          String ext = FilenameUtils.getExtension(path);
          if ("json".equals(ext)) {
            return com.sinnerschrader.smaller.lib.resource.Type.JSON;
          }
          if ("js".equals(ext) || "coffee".equals(ext)) {
            return com.sinnerschrader.smaller.lib.resource.Type.JS;
          }
          return com.sinnerschrader.smaller.lib.resource.Type.CSS;
        }

        @Override
        public String getContents() throws IOException {
          File file;
          if (path.startsWith(base.getAbsolutePath())) {
            file = new File(path);
          } else {
            file = new File(base, path);
          }
          return FileUtils.readFileToString(file);
        }
      };
    }

  }

}
