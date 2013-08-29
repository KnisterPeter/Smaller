package de.matrixweb.smaller.ui.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.application.ApplicationRunner;
import org.eclipse.rap.rwt.engine.RWTServlet;

import de.matrixweb.smaller.ui.base.internal.SmallerUiConfiguration;

/**
 * @author marwol
 */
public class RequestHandler {

  private ServletConfig config;

  private ApplicationRunner uiRunner;

  private RWTServlet uiServlet;

  /**
   * @param config
   * @throws ServletException
   */
  public void init(final ServletConfig config) throws ServletException {
    this.config = config;
    final ApplicationConfiguration configuration = new SmallerUiConfiguration();
    this.uiRunner = new ApplicationRunner(configuration, config.getServletContext());
    this.uiRunner.start();
    this.uiServlet = new RWTServlet();
    this.uiServlet.init(config);
  }

  /**
   * @param request
   * @param response
   * @throws IOException
   * @throws ServletException
   */
  public void serve(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    if ("/ui".equals(request.getServletPath())) {
      this.uiServlet.service(request, response);
    } else {
      System.out.println("RequestHandler.serve() " + request.getServletPath());
      InputStream in = this.config.getServletContext().getResourceAsStream(request.getServletPath());
      try {
        OutputStream out = response.getOutputStream();
        byte[] buf = new byte[1024];
        int len = in.read(buf, 0, 1024);
        while (len > -1) {
          out.write(buf, 0, len);
          len = in.read(buf, 0, 1024);
        }
        try {
        } finally {
          if (out != null) {
            out.close();
          }
        }
      } finally {
        if (in != null) {
          in.close();
        }
      }
    }
  }

  /**
   * 
   */
  public void destroy() {
    this.uiServlet.destroy();
    this.uiServlet = null;
    this.uiRunner.stop();
    this.uiRunner = null;
    this.config = null;
  }

  private static class UiWrappedRequest extends HttpServletRequestWrapper {

    /**
     * @param request
     */
    public UiWrappedRequest(final HttpServletRequest request) {
      super(request);
    }

    /**
     * @see javax.servlet.http.HttpServletRequestWrapper#getContextPath()
     */
    @Override
    public String getContextPath() {
      System.out.println("RequestHandler.UiWrappedRequest.getContextPath() " + super.getContextPath());
      return super.getContextPath();
    }

    /**
     * @see javax.servlet.http.HttpServletRequestWrapper#getPathTranslated()
     */
    @Override
    public String getPathTranslated() {
      System.out.println("RequestHandler.UiWrappedRequest.getPathTranslated() " + super.getPathTranslated());
      return super.getPathTranslated();
    }

    /**
     * @see javax.servlet.ServletRequestWrapper#getRealPath(java.lang.String)
     */
    @Override
    public String getRealPath(final String path) {
      System.out.println("RequestHandler.UiWrappedRequest.getRealPath() " + super.getRealPath(path));
      return super.getRealPath(path);
    }

    /**
     * @see javax.servlet.http.HttpServletRequestWrapper#getRequestURI()
     */
    @Override
    public String getRequestURI() {
      System.out.println("RequestHandler.UiWrappedRequest.getRequestURI() " + super.getRequestURI());
      return super.getRequestURI();
    }

    /**
     * @see javax.servlet.http.HttpServletRequestWrapper#getRequestURL()
     */
    @Override
    public StringBuffer getRequestURL() {
      System.out.println("RequestHandler.UiWrappedRequest.getRequestURL() " + super.getRequestURL());
      return super.getRequestURL();
    }

    /**
     * @see javax.servlet.http.HttpServletRequestWrapper#getServletPath()
     */
    @Override
    public String getServletPath() {
      return "/ui";
    }

  }

}
