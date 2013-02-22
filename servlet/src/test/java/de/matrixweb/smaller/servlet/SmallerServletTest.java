package de.matrixweb.smaller.servlet;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author marwol
 * 
 */
public class SmallerServletTest {

  private static ServerThread serverThread;

  /**
   * @throws Exception
   */
  @BeforeClass
  public static void startJetty() throws Exception {
    serverThread = new ServerThread();
    serverThread.start();
    final Thread t = new Thread(serverThread);
    t.start();
  }

  /**
   * @throws Exception
   */
  @AfterClass
  public static void stopJetty() throws Exception {
    serverThread.stop();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testCss() throws Exception {
    final URL url = new URL("http://localhost:65000/css/test.css");
    final InputStream in = url.openStream();
    try {
      final String body = IOUtils.toString(in);
      assertThat(body, is("a{color:blue}"));
    } finally {
      in.close();
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testJs() throws Exception {
    final URL url = new URL("http://localhost:65000/js/test.js");
    final InputStream in = url.openStream();
    try {
      final String body = IOUtils.toString(in);
      System.out.println("Expected: (function(){alert(\"a\")})();".replaceAll(
          "\n", "\\n"));
      System.out.println("Result  : " + body.replaceAll("\n", "\\n"));
      assertThat(body, is("(function(){alert(\"a\")})();"));
    } finally {
      in.close();
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testExisting() throws Exception {
    final URL url = new URL("http://localhost:65000/existing/test.js");
    final InputStream in = url.openStream();
    try {
      final String body = IOUtils.toString(in);
      System.out.println("Expected: // This should be the only response"
          .replaceAll("\n", "\\n"));
      System.out.println("Result  : " + body.replaceAll("\n", "\\n"));
      assertThat(body, is("// This should be the only response"));
    } finally {
      in.close();
    }
  }

  private static class ServerThread implements Runnable {

    private static Server jetty;

    public void start() {
      try {
        final MimeTypes mimeTypes = new MimeTypes();
        mimeTypes.addMimeMapping("css", "text/css");
        mimeTypes.addMimeMapping("js", "text/javascript");

        final ServletContextHandler cssContext = new ServletContextHandler(
            ServletContextHandler.SESSIONS);
        cssContext.setContextPath("/");
        cssContext.setBaseResource(Resource.newResource("src/test/resources"));
        cssContext.setMimeTypes(mimeTypes);
        ServletHolder holder = new ServletHolder(new SmallerServlet());
        holder.setInitParameter("processors", "lessjs:1.3.0,yuicompressor");
        holder.setInitParameter("includes", "less/test.less");
        // holder.setInitParameter("excludes", "css/b.css");
        cssContext.addServlet(holder, "/css/test.css");

        final ServletContextHandler jsContext = new ServletContextHandler(
            ServletContextHandler.SESSIONS);
        jsContext.setContextPath("/");
        jsContext.setBaseResource(Resource.newResource("src/test/resources"));
        holder = new ServletHolder(new SmallerServlet());
        holder.setInitParameter("processors", "closure");
        holder.setInitParameter("includes", "js/*.js");
        holder.setInitParameter("force", "true");
        holder.setInitParameter("mode", "lazy");
        jsContext.addServlet(holder, "/js/test.js");

        final ServletContextHandler existsContext = new ServletContextHandler(
            ServletContextHandler.SESSIONS);
        existsContext.setContextPath("/");
        existsContext.setBaseResource(Resource
            .newResource("src/test/resources"));
        holder = new ServletHolder(new SmallerServlet());
        holder.setInitParameter("processors", "merge");
        holder.setInitParameter("includes", "**/*.js");
        existsContext.addServlet(holder, "/existing/test.js");

        final HandlerCollection hc = new HandlerCollection();
        hc.addHandler(cssContext);
        hc.addHandler(jsContext);
        hc.addHandler(existsContext);

        jetty = new Server(65000);
        jetty.setHandler(hc);

        jetty.start();
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void run() {
      try {
        jetty.join();
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    }

    public void stop() {
      try {
        jetty.stop();
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    }

  }

}
