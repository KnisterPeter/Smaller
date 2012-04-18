package com.sinnerschrader.smaller;

import org.apache.camel.CamelContext;
import org.apache.camel.component.jetty.JettyHttpComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.LoggerFactory;

/**
 * @author marwol
 */
public class Server {

  private CamelContext camelContext;

  /**
   * @param args
   */
  public static void main(String[] args) {
    new Server().start(args);
  }

  /**
   * 
   */
  public Server() {
    camelContext = new DefaultCamelContext();
    camelContext.disableJMX();
    camelContext.addComponent("jetty", new JettyHttpComponent());
  }

  /**
   * @param args
   */
  public void start(String[] args) {
    try {
      camelContext.addRoutes(new Router(args));
      camelContext.start();
    } catch (Exception e) {
      LoggerFactory.getLogger(Server.class).error("CamelContext failed to start", e);
    }
  }

  /**
   * 
   */
  public void stop() {
    try {
      camelContext.stop();
    } catch (Exception e) {
      LoggerFactory.getLogger(Server.class).error("CamelContext failed to stop", e);
    }
  }

}
