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
import org.apache.camel.Property;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;
import org.codehaus.jackson.map.ObjectMapper;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.extensions.processor.css.SassCssProcessor;
import ro.isdc.wro.extensions.processor.css.YUICssCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.CoffeeScriptProcessor;
import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
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

import com.sinnerschrader.smaller.common.Manifest;
import com.sinnerschrader.smaller.common.Manifest.Task;
import com.sinnerschrader.smaller.cssembed.CssDataUriPostProcessor;
import com.sinnerschrader.smaller.less.ExtLessCssProcessor;

/**
 * @author marwol
 */
public class TaskHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskHandler.class);

  private CoffeeScriptProcessor coffeeScriptProcessor = new CoffeeScriptProcessor();

  private GoogleClosureCompressorProcessor googleClosureCompressorProcessor = new GoogleClosureCompressorProcessor();

  private UglifyJsProcessor uglifyJsProcessor = new UglifyJsProcessor();

  private SassCssProcessor sassCssProcessor = new SassCssProcessor();

  private YUICssCompressorProcessor yuiCssCompressorProcessor = new YUICssCompressorProcessor();

  /**
   * @param main
   * @return the route name of the next step
   */
  public String runTask(@Body Manifest main) {
    Task task = main.getNext();
    if (task == null) {
      LOGGER.info("Finished processing");
      return null;
    }
    String processor = StringUtils.capitalize(task.getProcessor().toLowerCase());
    if (processor.contains(",")) {
      processor = "Any";
    }
    String nextRoute = "direct:run" + processor;
    LOGGER.info("Next Route: {}", nextRoute);
    return nextRoute;
  }

  /**
   * @param input
   * @param output
   * @param main
   * @throws IOException
   */
  public void runAny(@Property(Router.PROP_INPUT) final File input, @Property(Router.PROP_OUTPUT) final File output, @Body Manifest main) throws IOException {
    LOGGER.debug("TaskHandler.runAny()");
    final Task task = main.getCurrent();
    runTool("js", task.getProcessor(), input, output, main);
    runTool("css", task.getProcessor(), input, output, main);
  }

  /**
   * @param input
   * @param output
   * @param main
   * @throws IOException
   */
  public void runCoffeeScript(@Property(Router.PROP_INPUT) final File input, @Property(Router.PROP_OUTPUT) final File output, @Body Manifest main)
      throws IOException {
    runJsTool("coffeeScript", input, output, main);
  }

  /**
   * @param input
   * @param output
   * @param main
   * @throws IOException
   */
  public void runClosure(@Property(Router.PROP_INPUT) final File input, @Property(Router.PROP_OUTPUT) final File output, @Body Manifest main)
      throws IOException {
    runJsTool("closure", input, output, main);
  }

  /**
   * @param input
   * @param output
   * @param main
   * @throws IOException
   */
  public void runUglifyJs(@Property(Router.PROP_INPUT) final File input, @Property(Router.PROP_OUTPUT) final File output, @Body Manifest main)
      throws IOException {
    runJsTool("uglifyjs", input, output, main);
  }

  /**
   * @param input
   * @param output
   * @param main
   * @throws IOException
   */
  public void runLessJs(@Property(Router.PROP_INPUT) final File input, @Property(Router.PROP_OUTPUT) final File output, @Body Manifest main) throws IOException {
    runCssTool("lessjs", input, output, main);
  }

  /**
   * @param input
   * @param output
   * @param main
   * @throws IOException
   */
  public void runSass(@Property(Router.PROP_INPUT) final File input, @Property(Router.PROP_OUTPUT) final File output, @Body Manifest main) throws IOException {
    runCssTool("sass", input, output, main);
  }

  /**
   * @param input
   * @param output
   * @param main
   * @throws IOException
   */
  public void runCssEmbed(@Property(Router.PROP_INPUT) final File input, @Property(Router.PROP_OUTPUT) final File output, @Body Manifest main)
      throws IOException {
    runCssTool("cssembed", input, output, main);
  }

  /**
   * @param input
   * @param output
   * @param main
   * @throws IOException
   */
  public void runYuiCompressor(@Property(Router.PROP_INPUT) final File input, @Property(Router.PROP_OUTPUT) final File output, @Body Manifest main)
      throws IOException {
    runCssTool("yuiCompressor", input, output, main);
  }

  private void runJsTool(final String tool, final File input, File output, Manifest main) throws IOException {
    runTool("js", tool, input, output, main);
  }

  private void runCssTool(final String tool, final File input, File output, Manifest main) throws IOException {
    runTool("css", tool, input, output, main);
  }

  private void runTool(final String type, final String tool, final File input, File output, final Manifest main) throws IOException {
    LOGGER.debug("TaskHandler.runTool('{}', '{}', '{}', {})", new Object[] { type, tool, input, main });
    final Task task = main.getCurrent();
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    spawnTool(60, new ThreadCallback() {
      @Override
      public void run() throws Exception {
        runInContext("all", type, baos, new Callback() {
          public void runWithContext(HttpServletRequest request, HttpServletResponse response) throws IOException {
            WroManagerFactory managerFactory = getManagerFactory(main, input, getWroModelFactory(task, input), filterPreProcessors(tool),
                filterPostProcessors(tool));
            managerFactory.create().process();
            managerFactory.destroy();
          }
        });
      }
    });
    String target = getOutputFile(task.getOut(), ResourceType.valueOf(type.toUpperCase()));
    FileUtils.writeByteArrayToFile(new File(output, target), baos.toByteArray());
    LOGGER.debug("TaskHandler.runTool('{}', '{}') => finished", type, tool);
  }

  private WroManagerFactory getManagerFactory(final Manifest manifest, final File input, WroModelFactory modelFactory, final String preProcessors,
      final String postProcessors) {
    ConfigurableStandaloneContextAwareManagerFactory cscamf = new ConfigurableStandaloneContextAwareManagerFactory() {
      @Override
      protected Properties createProperties() {
        Properties properties = new Properties();
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
        Map<String, ResourcePreProcessor> map = super.createPreProcessorsMap();
        map.put("lessjs", new ExtLessCssProcessor(manifest, input.getAbsolutePath()));
        map.put("sass", sassCssProcessor);
        return map;
      }

      /**
       * @see ro.isdc.wro.manager.factory.standalone.ConfigurableStandaloneContextAwareManagerFactory#createPostProcessorsMap()
       */
      @Override
      protected Map<String, ResourcePostProcessor> createPostProcessorsMap() {
        Map<String, ResourcePostProcessor> map = super.createPostProcessorsMap();
        map.put("coffeeScript", coffeeScriptProcessor);
        map.put("closure", googleClosureCompressorProcessor);
        map.put("uglifyjs", uglifyJsProcessor);
        map.put("yuiCompressor", yuiCssCompressorProcessor);
        map.put("cssembed", new CssDataUriPostProcessor(manifest, input.getAbsolutePath()));
        return map;
      }
    };
    StandaloneContext standaloneContext = new StandaloneContext();
    standaloneContext.setMinimize(true);
    standaloneContext.setContextFolder(input);
    cscamf.initialize(standaloneContext);
    cscamf.setModelFactory(modelFactory);
    return cscamf;
  }

  private String filterPreProcessors(String in) {
    List<String> processors = Arrays.asList("lessjs", "sass");
    List<String> list = new ArrayList<String>();
    for (String processor : in.split(",")) {
      if (processors.contains(processor)) {
        list.add(processor);
      }
    }
    return StringUtils.join(list, ',');
  }

  private String filterPostProcessors(String in) {
    List<String> processors = Arrays.asList("coffeeScript", "closure", "uglifyjs", "cssembed", "yuiCompressor");
    List<String> list = new ArrayList<String>();
    for (String processor : in.split(",")) {
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
    for (String s : task.getIn()) {
      String ext = FilenameUtils.getExtension(s);
      if ("json".equals(ext)) {
        ObjectMapper om = new ObjectMapper();
        input.addAll(Arrays.asList(om.readValue(new File(base, s), String[].class)));
      } else {
        input.add(s);
      }
    }
    return new WroModelFactory() {

      public WroModel create() {
        Group group = new Group("all");
        for (String i : input) {
          group.addResource(Resource.create(new File(base, i).toURI().toString(), Utils.getResourceType(i)));
        }
        return new WroModel().addGroup(group);
      }

      public void destroy() {
      }
    };
  }

  private String getOutputFile(String[] files, ResourceType type) {
    for (String file : files) {
      if (Utils.getResourceType(file) == type) {
        return file;
      }
    }
    throw new RuntimeException("No output file specified for type " + type);
  }

  private void spawnTool(long timeout, final ThreadCallback callback) {
    final List<Exception> holder = new ArrayList<Exception>(1);
    final CountDownLatch latch = new CountDownLatch(1);
    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          callback.run();
        } catch (Exception e) {
          holder.add(e);
        }
        latch.countDown();
      }
    }).start();
    try {
      if (!latch.await(timeout, TimeUnit.SECONDS)) {
        throw new RuntimeException("Tool timeout");
      }
      if (!holder.isEmpty()) {
        throw new UnhandledException(holder.get(0));
      }
    } catch (InterruptedException e) {
      LOGGER.warn("Interrupted tool thread", e);
    }
  }

  private void runInContext(String group, String type, OutputStream out, Callback callback) throws IOException {
    Context.set(Context.standaloneContext());
    try {
      HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
      Mockito.when(request.getRequestURI()).thenReturn(group + '.' + type);

      HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
      Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(out));

      final WroConfiguration config = new WroConfiguration();
      config.setParallelPreprocessing(false);

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

    void run() throws Exception;

  }

  private interface Callback {

    void runWithContext(HttpServletRequest request, HttpServletResponse response) throws IOException;
  }

}
