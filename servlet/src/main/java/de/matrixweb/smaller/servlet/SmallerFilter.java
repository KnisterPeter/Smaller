package de.matrixweb.smaller.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author marwol
 */
public class SmallerFilter implements Filter {

  private EmbeddedSmaller smaller;

  /**
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
    this.smaller = new EmbeddedSmaller(filterConfig);
    this.smaller.init();
  }

  /**
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
   *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(final ServletRequest request,
      final ServletResponse response, final FilterChain chain)
      throws IOException, ServletException {
    this.smaller.execute((HttpServletRequest) request,
        (HttpServletResponse) response);
  }

  /**
   * @see javax.servlet.Filter#destroy()
   */
  @Override
  public void destroy() {
  }

}
