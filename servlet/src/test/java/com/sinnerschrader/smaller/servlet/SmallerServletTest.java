package com.sinnerschrader.smaller.servlet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  @BeforeClass
  public static void startJetty() throws Exception {
    serverThread = new ServerThread();
    serverThread.start();
    Thread t = new Thread(serverThread);
    t.start();
  }

  @AfterClass
  public static void stopJetty() throws Exception {
    serverThread.stop();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testCss() throws Exception {
    URL url = new URL("http://localhost:65000/css/test.css");
    InputStream in = url.openStream();
    try {
      String body = IOUtils.toString(in);
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
    URL url = new URL("http://localhost:65000/js/test.js");
    InputStream in = url.openStream();
    try {
      String body = IOUtils.toString(in);
      assertThat(body, is("window={location:{href:\"\",port:\"\"}};location=window.location;document={getElementById:function(){return{childNodes:[],style:{},appendChild:function(){}}},getElementsByTagName:function(){return[]},createElement:function(){return{style:{}}},createTextNode:function(){return{}}};window.XMLHttpRequest=function(){this.status=200;this.resource=this.url=null};window.XMLHttpRequest.prototype.open=function(b,a){this.url=a};window.XMLHttpRequest.prototype.setRequestHeader=function(){};\nwindow.XMLHttpRequest.prototype.send=function(){this.responseText=new String(resolver.resolve(this.url).getContents())};window.XMLHttpRequest.prototype.getResponseHeader=function(){};XMLHttpRequest=window.XMLHttpRequest;lessIt=function(b){var a;(new window.less.Parser({optimization:1})).parse(b,function(c,d){if(c)throw c.message;a=b;a=d.toCSS()});return a};"));
    } finally {
      in.close();
    }
  }

  private static class ServerThread implements Runnable {

    private static Server jetty;

    public void start() {
      try {
        MimeTypes mimeTypes = new MimeTypes();
        mimeTypes.addMimeMapping("css", "text/css");
        mimeTypes.addMimeMapping("js", "text/javascript");
        
        ServletContextHandler cssContext = new ServletContextHandler(
            ServletContextHandler.SESSIONS);
        cssContext.setContextPath("/css");
        cssContext.setBaseResource(Resource.newResource("src/test/resources"));
        cssContext.setMimeTypes(mimeTypes);
        ServletHolder holder = new ServletHolder(new SmallerServlet());
        holder.setInitParameter("processors", "lessjs,yuicompressor");
        holder.setInitParameter("includes", "**/*.css");
        holder.setInitParameter("excludes", "css/b.css");
        cssContext.addServlet(holder, "/test.css");

        ServletContextHandler jsContext = new ServletContextHandler(
            ServletContextHandler.SESSIONS);
        jsContext.setContextPath("/js");
        String cp = System.getProperty("java.class.path");
        Matcher matcher = Pattern.compile(
            ".*:(.*lib-\\d+.\\d+.\\d+(?:-SNAPSHOT).jar).*").matcher(cp);
        if (matcher.matches()) {
          jsContext.setBaseResource(Resource.newResource("jar:file:"
              + matcher.group(1) + "!/"));
        } else {
          matcher = Pattern.compile(".*:(.*lib/target/classes).*").matcher(cp);
          matcher.matches();
          jsContext.setBaseResource(Resource.newResource("file:"
              + matcher.group(1)));
        }
        holder = new ServletHolder(new SmallerServlet());
        holder.setInitParameter("processors", "closure");
        holder.setInitParameter("includes", "com/sinnerschrader/**/*.js");
        holder.setInitParameter("excludes", "**/less-1.3.0.js,**/run.js");
        jsContext.addServlet(holder, "/test.js");

        HandlerCollection hc = new HandlerCollection();
        hc.addHandler(cssContext);
        hc.addHandler(jsContext);

        jetty = new Server(65000);
        jetty.setHandler(hc);

        jetty.start();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void run() {
      try {
        jetty.join();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    public void stop() {
      try {
        jetty.stop();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

  }

}
