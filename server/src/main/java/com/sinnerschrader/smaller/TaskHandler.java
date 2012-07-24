package com.sinnerschrader.smaller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.camel.Body;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.extensions.processor.css.SassCssProcessor;
import ro.isdc.wro.extensions.processor.css.YUICssCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.CoffeeScriptProcessor;
import ro.isdc.wro.extensions.processor.js.UglifyJsProcessor;
import ro.isdc.wro.http.support.DelegatingServletOutputStream;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.manager.factory.standalone.ConfigurableStandaloneContextAwareManagerFactory;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory;

import com.sinnerschrader.smaller.closure.ClosureCompressorProcessor;
import com.sinnerschrader.smaller.common.Manifest;
import com.sinnerschrader.smaller.common.Manifest.Task;
import com.sinnerschrader.smaller.cssembed.CssDataUriPostProcessor;
import com.sinnerschrader.smaller.less.ExtLessCssProcessor;

/**
 * @author marwol
 */
public class TaskHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskHandler.class);

  private static final int TOOL_TIMEOUT = 60 * 5;

  /**
   * @param context
   * @return the route name of the next step
   */
  public String runTask(@Body final RequestContext context) {
    final Task task = context.getManifest().getNext();
    if (task == null) {
      LOGGER.info("Finished processing");
      return null;
    }
    String processor = StringUtils.capitalize(task.getProcessor().toLowerCase());
    if (processor.contains(",")) {
      processor = "Any";
    }
    final String nextRoute = "direct:run" + processor;
    LOGGER.info("Next Route: {}", nextRoute);
    return nextRoute;
  }

  /**
   * @param context
   * @throws IOException
   */
  public void runAny(@Body final RequestContext context) throws IOException {
    LOGGER.debug("TaskHandler.runAny()");
    final Manifest main = context.getManifest();
    final File input = context.getInput();
    final File output = context.getOutput();
    final Task task = main.getCurrent();
    try {
      this.runTool("js", task.getProcessor(), input, output, main);
    } catch (final SkipOutputTypeException e1) {
      LOGGER.warn("Skipped 'anyJs', since no output file for js was specified.");
    }
    try {
      this.runTool("css", task.getProcessor(), input, output, main);
    } catch (final SkipOutputTypeException e) {
      LOGGER.warn("Skipped 'anyCss', since no output file for css was specified.");
    }
  }

  /**
   * @param context
   * @throws IOException
   */
  public void runCoffeeScript(@Body final RequestContext context) throws IOException {
    try {
      this.runJsTool("coffeeScript", context.getInput(), context.getOutput(), context.getManifest());
    } catch (final SkipOutputTypeException e) {
      LOGGER.warn("Skipped 'coffeeScript', since no output file for js was specified.");
    }
  }

  /**
   * @param context
   * @throws IOException
   */
  public void runClosure(@Body final RequestContext context) throws IOException {
    try {
      this.runJsTool("closure", context.getInput(), context.getOutput(), context.getManifest());
    } catch (final SkipOutputTypeException e) {
      LOGGER.warn("Skipped 'closure', since no output file for js was specified.");
    }
  }

  /**
   * @param context
   * @throws IOException
   */
  public void runUglifyJs(@Body final RequestContext context) throws IOException {
    try {
      this.runJsTool("uglifyjs", context.getInput(), context.getOutput(), context.getManifest());
    } catch (final SkipOutputTypeException e) {
      LOGGER.warn("Skipped 'uglifyjs', since no output file for js was specified.");
    }
  }

  /**
   * @param context
   * @throws IOException
   */
  public void runLessJs(@Body final RequestContext context) throws IOException {
    try {
      this.runCssTool("lessjs", context.getInput(), context.getOutput(), context.getManifest());
    } catch (final SkipOutputTypeException e) {
      LOGGER.warn("Skipped 'lessjs', since no output file for css was specified.");
    }
  }

  /**
   * @param context
   * @throws IOException
   */
  public void runSass(@Body final RequestContext context) throws IOException {
    try {
      this.runCssTool("sass", context.getInput(), context.getOutput(), context.getManifest());
    } catch (final SkipOutputTypeException e) {
      LOGGER.warn("Skipped 'sass', since no output file for css was specified.");
    }
  }

  /**
   * @param context
   * @throws IOException
   */
  public void runCssEmbed(@Body final RequestContext context) throws IOException {
    try {
      this.runCssTool("cssembed", context.getInput(), context.getOutput(), context.getManifest());
    } catch (final SkipOutputTypeException e) {
      LOGGER.warn("Skipped 'cssembed', since no output file for css was specified.");
    }
  }

  /**
   * @param context
   * @throws IOException
   */
  public void runYuiCompressor(@Body final RequestContext context) throws IOException {
    try {
      this.runCssTool("yuiCompressor", context.getInput(), context.getOutput(), context.getManifest());
    } catch (final SkipOutputTypeException e) {
      LOGGER.warn("Skipped 'yuiCompressor', since no output file for css was specified.");
    }
  }

  private void runJsTool(final String tool, final File input, final File output, final Manifest main) throws IOException, SkipOutputTypeException {
    this.runTool("js", tool, input, output, main);
  }

  private void runCssTool(final String tool, final File input, final File output, final Manifest main) throws IOException, SkipOutputTypeException {
    this.runTool("css", tool, input, output, main);
  }

  private void runTool(final String type, final String tool, final File input, final File output, final Manifest main) throws IOException,
      SkipOutputTypeException {
    LOGGER.debug("TaskHandler.runTool('{}', '{}', '{}', {})", new Object[] { type, tool, input, main });
    final Task task = main.getCurrent();
    final String target = this.getOutputFile(task.getOut(), ResourceType.valueOf(type.toUpperCase()));
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    this.spawnTool(TOOL_TIMEOUT, new ThreadCallback() {
      @Override
      public void run() {
        try {
          TaskHandler.this.runInContext("all", type, baos, new Callback() {
            @Override
            public void runWithContext(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
              final WroManagerFactory managerFactory = TaskHandler.this.getManagerFactory(main, input, TaskHandler.this.getWroModelFactory(task, input),
                  TaskHandler.this.filterPreProcessors(tool), TaskHandler.this.filterPostProcessors(tool));
              managerFactory.create().process();
              managerFactory.destroy();
            }
          });
        } catch (final IOException e) {
          throw new TaskException("Error during task execution (" + tool + ")", e);
        }
      }
    });
    FileUtils.writeByteArrayToFile(new File(output, target), baos.toByteArray());
    LOGGER.debug("TaskHandler.runTool('{}', '{}') => finished", type, tool);
  }

  private WroManagerFactory getManagerFactory(final Manifest manifest, final File input, final WroModelFactory modelFactory, final String preProcessors,
      final String postProcessors) {
    final ConfigurableStandaloneContextAwareManagerFactory cscamf = new CustomManagerFactory(manifest, input, preProcessors, postProcessors);
    final StandaloneContext standaloneContext = new StandaloneContext();
    standaloneContext.setMinimize(true);
    standaloneContext.setContextFolder(input);
    cscamf.initialize(standaloneContext);
    cscamf.setModelFactory(modelFactory);
    return cscamf;
  }

  private String filterPreProcessors(final String in) {
    final List<String> processors = Arrays.asList("lessjs", "sass");
    final List<String> list = new ArrayList<String>();
    for (final String processor : in.split(",")) {
      if (processors.contains(processor)) {
        list.add(processor);
      }
    }
    return StringUtils.join(list, ',');
  }

  private String filterPostProcessors(final String in) {
    final List<String> processors = Arrays.asList("coffeeScript", "closure", "uglifyjs", "cssembed", "yuiCompressor");
    final List<String> list = new ArrayList<String>();
    for (final String processor : in.split(",")) {
      if (processors.contains(processor)) {
        list.add(processor);
      }
    }
    return StringUtils.join(list, ',');
  }

  /**
   * @param base
   * @return a wro model with one group 'all' and all input parameters
   * @throws IOException
   */
  private WroModelFactory getWroModelFactory(final Task task, final File base) throws IOException {
    final List<String> input = new ArrayList<String>();
    for (final String s : task.getIn()) {
      final String ext = FilenameUtils.getExtension(s);
      if ("json".equals(ext)) {
        final ObjectMapper om = new ObjectMapper();
        input.addAll(Arrays.asList(om.readValue(new File(base, s), String[].class)));
      } else {
        input.add(s);
      }
    }
    return new WroModelFactory() {

      @Override
      public WroModel create() {
        final Group group = new Group("all");
        for (final String i : input) {
          group.addResource(Resource.create(new File(base, i).toURI().toString(), Utils.getResourceType(i)));
        }
        return new WroModel().addGroup(group);
      }

      @Override
      public void destroy() {
      }
    };
  }

  private String getOutputFile(final String[] files, final ResourceType type) throws SkipOutputTypeException {
    for (final String file : files) {
      if (Utils.getResourceType(file) == type) {
        return file;
      }
    }
    throw new SkipOutputTypeException();
  }

  private void spawnTool(final long timeout, final ThreadCallback callback) {
    final List<RuntimeException> holder = new ArrayList<RuntimeException>(1);
    final CountDownLatch latch = new CountDownLatch(1);
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          callback.run();
        } catch (final RuntimeException e) {
          holder.add(e);
        }
        latch.countDown();
      }
    }).start();
    try {
      if (!latch.await(timeout, TimeUnit.SECONDS)) {
        throw new TaskException("Tool timeout");
      }
      if (!holder.isEmpty()) {
        throw holder.get(0);
      }
    } catch (final InterruptedException e) {
      LOGGER.warn("Interrupted tool thread", e);
    }
  }

  private void runInContext(final String group, final String type, final OutputStream out, final Callback callback) throws IOException {
    Context.set(Context.standaloneContext());
    try {
      final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
      Mockito.when(request.getRequestURI()).thenReturn(group + '.' + type);

      final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
      Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(out));

      final WroConfiguration config = new WroConfiguration();
      config.setParallelPreprocessing(false);
      config.setEncoding("UTF-8");

      Context.set(Context.webContext(request, response, Mockito.mock(FilterConfig.class)), config);
      try {
        callback.runWithContext(request, response);
      } finally {
        Context.unset();
      }
    } finally {
      Context.unset();
    }
  }

  private interface ThreadCallback {

    void run();

  }

  private interface Callback {

    void runWithContext(HttpServletRequest request, HttpServletResponse response) throws IOException;
  }

  private class SkipOutputTypeException extends Exception {
    private static final long serialVersionUID = -5732545354570277937L;
  }

  private static class CustomManagerFactory extends ConfigurableStandaloneContextAwareManagerFactory {

    private final Manifest manifest;

    private final File input;

    private final String preProcessors;

    private final String postProcessors;

    CustomManagerFactory(final Manifest manifest, final File input, final String preProcessors, final String postProcessors) {
      this.manifest = manifest;
      this.input = input;
      this.preProcessors = preProcessors;
      this.postProcessors = postProcessors;
    }

    @Override
    protected Properties createProperties() {
      final Properties properties = new Properties();
      if (StringUtils.isNotBlank(preProcessors)) {
        properties.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, preProcessors);
      }
      if (StringUtils.isNotBlank(postProcessors)) {
        properties.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, postProcessors);
      }
      return properties;
    }

    @Override
    protected Map<String, ResourcePreProcessor> createPreProcessorsMap() {
      final Map<String, ResourcePreProcessor> map = super.createPreProcessorsMap();
      map.put("lessjs", new ExtLessCssProcessor(manifest, input.getAbsolutePath()));
      map.put("sass", new SassCssProcessor());
      return map;
    }

    /**
     * @see ro.isdc.wro.manager.factory.standalone.ConfigurableStandaloneContextAwareManagerFactory#createPostProcessorsMap()
     */
    @Override
    protected Map<String, ResourcePostProcessor> createPostProcessorsMap() {
      final Map<String, ResourcePostProcessor> map = super.createPostProcessorsMap();
      map.put("coffeeScript", new CoffeeScriptProcessor());
      map.put("closure", new ClosureCompressorProcessor());
      map.put("uglifyjs", new UglifyJsProcessor());
      map.put("yuiCompressor", new YUICssCompressorProcessor());
      map.put("cssembed", new CssDataUriPostProcessor(manifest, input.getAbsolutePath()));
      return map;
    }

  }

}
