package de.matrixweb.smaller.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;


import de.matrixweb.smaller.common.Task;
import de.matrixweb.smaller.pipeline.Pipeline;
import de.matrixweb.smaller.pipeline.Result;
import de.matrixweb.smaller.resource.impl.JavaEEProcessorFactory;

/**
 * @author marwol
 */
public class SmallerFilter implements Filter {

  private FilterConfig filterConfig;

  private Result result;

  /**
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
    this.filterConfig = filterConfig;
    if (!isDevelopment()) {
      process();
    }
  }

  /**
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
   *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(final ServletRequest req,
      final ServletResponse response, final FilterChain chain)
      throws IOException, ServletException {
    if (isDevelopment()) {
      process();
    }
    final HttpServletRequest request = (HttpServletRequest) req;
    String contentType = request.getContentType();
    if (contentType == null) {
      if (request.getRequestURI().endsWith("js")) {
        contentType = "text/javascript";
      } else if (request.getRequestURI().endsWith("css")) {
        contentType = "text/css";
      }
    }
    response.setContentType(contentType);
    final PrintWriter writer = response.getWriter();
    if ("text/javascript".equals(contentType)) {
      writer.print(this.result.getJs().getContents());
    } else if ("text/css".equals(contentType)) {
      writer.print(this.result.getCss().getContents());
    }
    writer.close();
  }

  /**
   * @see javax.servlet.Filter#destroy()
   */
  @Override
  public void destroy() {
  }

  private String getInitParameter(final String name) {
    return this.filterConfig.getInitParameter(name);
  }

  private boolean isDevelopment() {
    return "development".equals(getInitParameter("mode"));
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
    final Set<String> resources = new ResourceScanner(
        this.filterConfig.getServletContext(), includes.split("[, ]"),
        excludes != null ? excludes.split("[, ]") : new String[] {})
        .getResources();

    final Task task = new Task();
    task.setProcessor(processors);
    task.setIn(resources.toArray(new String[resources.size()]));
    this.result = new Pipeline(new JavaEEProcessorFactory()).execute(
        new ServletContextResourceResolver(this.filterConfig
            .getServletContext()), task);
  }

}
