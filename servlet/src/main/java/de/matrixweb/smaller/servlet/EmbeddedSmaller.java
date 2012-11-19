package de.matrixweb.smaller.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Set;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import de.matrixweb.smaller.common.Task;
import de.matrixweb.smaller.pipeline.Pipeline;
import de.matrixweb.smaller.pipeline.Result;
import de.matrixweb.smaller.resource.impl.JavaEEProcessorFactory;

/**
 * @author markusw
 */
public class EmbeddedSmaller {

  private FilterConfig filterConfig;

  private ServletConfig servletConfig;

  private Result result;

  /**
   * @param filterConfig
   */
  public EmbeddedSmaller(final FilterConfig filterConfig) {
    this.filterConfig = filterConfig;
  }

  /**
   * @param servletConfig
   */
  public EmbeddedSmaller(final ServletConfig servletConfig) {
    this.servletConfig = servletConfig;
  }

  /**
   * @throws ServletException
   */
  public void init() throws ServletException {
    if (!isDevelopment() && !isLazy()) {
      process();
    }
  }

  /**
   * @param request
   * @param response
   * @throws ServletException
   * @throws IOException
   */
  public void execute(final HttpServletRequest request,
      final HttpServletResponse response) throws ServletException, IOException {
    final boolean exists = exists(request) && !isForce();
    if (!exists && (isDevelopment() || requireLazyRun())) {
      process();
    }
    final String contentType = getContentType(request);
    response.setContentType(contentType);
    final PrintWriter writer = response.getWriter();
    writer.print(getContent(request, exists, contentType));
    writer.close();
  }

  private boolean requireLazyRun() {
    return isLazy() && this.result == null;
  }

  private boolean exists(final HttpServletRequest request)
      throws MalformedURLException {
    return getServletContext().getResource(request.getRequestURI()) != null;
  }

  private String getContentType(final HttpServletRequest request) {
    String contentType = request.getContentType();
    if (contentType == null) {
      if (request.getRequestURI().endsWith("js")) {
        contentType = "text/javascript";
      } else if (request.getRequestURI().endsWith("css")) {
        contentType = "text/css";
      }
    }
    return contentType;
  }

  private String getContent(final HttpServletRequest request,
      final boolean exists, final String contentType) throws IOException {
    if (exists) {
      final InputStream in = getServletContext().getResourceAsStream(
          request.getRequestURI());
      try {
        return IOUtils.toString(in);
      } finally {
        in.close();
      }
    }
    return this.result.get(contentType).getContents();
  }

  private String getInitParameter(final String name) {
    if (this.servletConfig != null) {
      return this.servletConfig.getInitParameter(name);
    }
    return this.filterConfig.getInitParameter(name);
  }

  private ServletContext getServletContext() {
    if (this.servletConfig != null) {
      return this.servletConfig.getServletContext();
    }
    return this.filterConfig.getServletContext();
  }

  private boolean isForce() {
    return "true".equals(getInitParameter("force"));
  }

  private boolean isDevelopment() {
    return "development".equals(getInitParameter("mode"));
  }

  private boolean isLazy() {
    return "lazy".equals(getInitParameter("mode"));
  }

  private void process() throws ServletException {
    final String processors = getInitParameter("processors");
    if (processors == null) {
      throw new ServletException("init-param 'processors' must be configured");
    }
    final String includes = getInitParameter("includes");
    if (StringUtils.isBlank(includes)) {
      throw new ServletException("init-param 'includes' must be configured");
    }
    final String excludes = getInitParameter("excludes");
    final Set<String> resources = new ResourceScanner(getServletContext(),
        includes.split("[, ]"), excludes != null ? excludes.split("[, ]")
            : new String[] {}).getResources();
    final String options = getInitParameter("options");

    final Task task = new Task();
    task.setProcessor(processors);
    task.setIn(resources.toArray(new String[resources.size()]));
    task.setOptionsDefinition(options);
    this.result = new Pipeline(new JavaEEProcessorFactory()).execute(
        new ServletContextResourceResolver(getServletContext()), task);
  }

}
