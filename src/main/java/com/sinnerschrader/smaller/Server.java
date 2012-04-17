package com.sinnerschrader.smaller;

import org.apache.camel.CamelContext;
import org.apache.camel.component.jetty.JettyHttpComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.LoggerFactory;

/**
 * @author marwol
 */
public class Server {

  /**
   * @param args
   */
  public static void main(String[] args) {
    CamelContext camelContext = new DefaultCamelContext();
    camelContext.disableJMX();
    camelContext.addComponent("jetty", new JettyHttpComponent());
    try {
      camelContext.addRoutes(new Router(args));
      camelContext.start();
    } catch (Exception e) {
      LoggerFactory.getLogger(Server.class).error("CamelContext failed to start", e);
    }
  }

}
