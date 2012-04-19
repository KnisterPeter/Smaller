package com.sinnerschrader.smaller;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.extensions.processor.js.UglifyJsProcessor;
import ro.isdc.wro.http.support.DelegatingServletOutputStream;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.manager.factory.standalone.ConfigurableStandaloneContextAwareManagerFactory;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory;

import com.sinnerschrader.smaller.Manifest.Task;

/**
 * @author marwol
 */
public class TaskHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskHandler.class);

  private GoogleClosureCompressorProcessor googleClosureCompressorProcessor = new GoogleClosureCompressorProcessor();

  private UglifyJsProcessor uglifyJsProcessor = new UglifyJsProcessor();

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
    String nextRoute = "direct:run" + StringUtils.capitalize(task.getProcessor().toLowerCase());
    LOGGER.info("Next Route: {}", nextRoute);
    return nextRoute;
  }

  /**
   * @param base
   * @param main
   * @throws Exception
   */
  public void runClosure(final @Property(Router.PROP_DIRECTORY) File base, @Body Manifest main) throws Exception {
    final Task task = main.getCurrent();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    runInContext("all", "js", baos, new Callback() {
      public void runWithContext(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WroManagerFactory managerFactory = getManagerFactory(task.getWroModelFactory(base), GoogleClosureCompressorProcessor.ALIAS_SIMPLE, null);
        managerFactory.create().process();
        managerFactory.destroy();
      }
    });
    FileUtils.writeByteArrayToFile(new File(base, task.getOut()[0]), baos.toByteArray());
  }

  /**
   * @param base
   * @param main
   * @throws Exception
   */
  public void runUglifyJs(final @Property(Router.PROP_DIRECTORY) File base, @Body Manifest main) throws Exception {
    final Task task = main.getCurrent();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    runInContext("all", "js", baos, new Callback() {
      public void runWithContext(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WroManagerFactory managerFactory = getManagerFactory(task.getWroModelFactory(base), UglifyJsProcessor.ALIAS_UGLIFY, null);
        managerFactory.create().process();
        managerFactory.destroy();
      }
    });
    FileUtils.writeByteArrayToFile(new File(base, task.getOut()[0]), baos.toByteArray());
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
        map.put(GoogleClosureCompressorProcessor.ALIAS_SIMPLE, googleClosureCompressorProcessor);
        map.put(UglifyJsProcessor.ALIAS_UGLIFY, uglifyJsProcessor);
        return map;
      }
    };
    StandaloneContext standaloneContext = new StandaloneContext();
    standaloneContext.setMinimize(true);
    cscamf.initialize(standaloneContext);
    cscamf.setModelFactory(modelFactory);
    return cscamf;
  }

  private void runInContext(String group, String type, OutputStream out, Callback callback) throws Exception {
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

    void runWithContext(HttpServletRequest request, HttpServletResponse response) throws Exception;
  }

}
