package com.sinnerschrader.smaller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.camel.Body;
import org.apache.camel.Property;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.extensions.processor.css.SassCssProcessor;
import ro.isdc.wro.extensions.processor.css.YUICssCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.CoffeeScriptProcessor;
import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.UglifyJsProcessor;
import ro.isdc.wro.http.support.DelegatingServletOutputStream;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.manager.factory.standalone.ConfigurableStandaloneContextAwareManagerFactory;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.css.CssDataUriPreProcessor;

import com.sinnerschrader.smaller.Manifest.Task;

/**
 * @author marwol
 */
public class TaskHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskHandler.class);

  private CoffeeScriptProcessor coffeeScriptProcessor = new CoffeeScriptProcessor();

  private GoogleClosureCompressorProcessor googleClosureCompressorProcessor = new GoogleClosureCompressorProcessor();

  private UglifyJsProcessor uglifyJsProcessor = new UglifyJsProcessor();

  private LessCssProcessor lessCssProcessor = new LessCssProcessor();

  private SassCssProcessor sassCssProcessor = new SassCssProcessor();

  private CssDataUriPreProcessor cssDataUriPreProcessor = new CssDataUriPreProcessor();

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
   * @param base
   * @param main
   * @throws IOException
   */
  public void runAny(@Property(Router.PROP_DIRECTORY) final File base, @Body Manifest main) throws IOException {
    final Task task = main.getCurrent();
    runTool(0, "js", task.getProcessor(), base, main);
    runTool(1, "css", task.getProcessor(), base, main);
  }

  /**
   * @param base
   * @param main
   * @throws IOException
   */
  public void runCoffeeScript(@Property(Router.PROP_DIRECTORY) final File base, @Body Manifest main) throws IOException {
    runJsTool("coffeeScript", base, main);
  }

  /**
   * @param base
   * @param main
   * @throws IOException
   */
  public void runClosure(@Property(Router.PROP_DIRECTORY) final File base, @Body Manifest main) throws IOException {
    runJsTool("closure", base, main);
  }

  /**
   * @param base
   * @param main
   * @throws IOException
   */
  public void runUglifyJs(@Property(Router.PROP_DIRECTORY) final File base, @Body Manifest main) throws IOException {
    runJsTool("uglifyjs", base, main);
  }

  /**
   * @param base
   * @param main
   * @throws IOException
   */
  public void runLessJs(@Property(Router.PROP_DIRECTORY) final File base, @Body Manifest main) throws IOException {
    runCssTool("lessjs", base, main);
  }

  /**
   * @param base
   * @param main
   * @throws IOException
   */
  public void runSass(@Property(Router.PROP_DIRECTORY) final File base, @Body Manifest main) throws IOException {
    runCssTool("sass", base, main);
  }

  /**
   * @param base
   * @param main
   * @throws IOException
   */
  public void runCssEmbed(@Property(Router.PROP_DIRECTORY) final File base, @Body Manifest main) throws IOException {
    runCssTool("cssembed", base, main);
  }

  /**
   * @param base
   * @param main
   * @throws IOException
   */
  public void runYuiCompressor(@Property(Router.PROP_DIRECTORY) final File base, @Body Manifest main) throws IOException {
    runCssTool("yuiCompressor", base, main);
  }

  private void runJsTool(final String tool, final File base, Manifest main) throws IOException {
    runTool("js", tool, base, main);
  }

  private void runCssTool(final String tool, final File base, Manifest main) throws IOException {
    runTool("css", tool, base, main);
  }

  private void runTool(String type, final String tool, final File base, Manifest main) throws IOException {
    runTool(0, type, tool, base, main);
  }

  private void runTool(int storeIndex, String type, final String tool, final File base, Manifest main) throws IOException {
    final Task task = main.getCurrent();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    runInContext("all", type, baos, new Callback() {
      public void runWithContext(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WroManagerFactory managerFactory = getManagerFactory(task.getWroModelFactory(base), tool, null);
        managerFactory.create().process();
        managerFactory.destroy();
      }
    });
    FileUtils.writeByteArrayToFile(new File(base, task.getOut()[storeIndex]), baos.toByteArray());
  }

  private WroManagerFactory getManagerFactory(WroModelFactory modelFactory, final String preProcessors, final String postProcessors) {
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
        map.put("coffeeScript", coffeeScriptProcessor);
        map.put("uglifyjs", uglifyJsProcessor);
        map.put("lessjs", lessCssProcessor);
        map.put("sass", sassCssProcessor);
        map.put("cssembed", cssDataUriPreProcessor);
        map.put("closure", googleClosureCompressorProcessor);
        map.put("yuiCompressor", yuiCssCompressorProcessor);
        return map;
      }
    };
    StandaloneContext standaloneContext = new StandaloneContext();
    standaloneContext.setMinimize(true);
    cscamf.initialize(standaloneContext);
    cscamf.setModelFactory(modelFactory);
    return cscamf;
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

  private interface Callback {

    void runWithContext(HttpServletRequest request, HttpServletResponse response) throws IOException;
  }

}
