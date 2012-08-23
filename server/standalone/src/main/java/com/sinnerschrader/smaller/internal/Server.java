package com.sinnerschrader.smaller.internal;

import java.io.InputStream;
import java.net.InetSocketAddress;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.sinnerschrader.smaller.osgi.http.Servlet;
import com.sinnerschrader.smaller.pipeline.Pipeline;
import com.sinnerschrader.smaller.resource.impl.JavaEEProcessorFactory;

/**
 * @author marwol
 */
public class Server {

  private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

  private String version;

  private final org.eclipse.jetty.server.Server server;

  /**
   * @param args
   */
  public static void main(final String[] args) {
    new Server(args).start();
  }

  /**
   * @param args
   */
  public Server(final String... args) {
    final ListenAddress la = new ListenAddress(args);
    LOGGER.info("\nVersion: {}\nListen On: {}", getVersion(), la);
    this.server = new org.eclipse.jetty.server.Server(
        InetSocketAddress.createUnresolved(la.getHost(), la.getPort()));
    final ServletHandler handler = new ServletHandler();
    handler.addServletWithMapping(new ServletHolder(new Servlet(new Pipeline(
        new JavaEEProcessorFactory()))), "/");
    this.server.setHandler(handler);
  }

  /**
   * 
   */
  public void start() {
    try {
      this.server.start();
      this.server.join();
    } catch (final Exception e) {
      LoggerFactory.getLogger(Server.class).error(
          "Failed to start jetty server", e);
    }
  }

  /**
   * 
   */
  public void stop() {
    try {
      this.server.stop();
    } catch (final Exception e) {
      LoggerFactory.getLogger(Server.class).error(
          "Failed to stop jetty server", e);
    }
  }

  private String getVersion() {
    if (this.version != null) {
      return this.version;
    }
    synchronized (this) {
      if (this.version != null) {
        return this.version;
      }
      String v = "Smaller(development)";
      final InputStream is = Server.class.getClassLoader().getResourceAsStream(
          "META-INF/maven/com.sinnerschrader.smaller/server/pom.xml");
      if (is != null) {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc;
        try {
          final DocumentBuilder db = dbf.newDocumentBuilder();
          doc = db.parse(is);
          v = "Smaller("
              + doc.getElementsByTagName("version").item(0).getTextContent()
              + ")";
        } catch (final Exception e) {
          LOGGER.warn("Failed to get version info from pom", e);
        }
      }
      this.version = v;
    }
    return this.version;
  }

}
