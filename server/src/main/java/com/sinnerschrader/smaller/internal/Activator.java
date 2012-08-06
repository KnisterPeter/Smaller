package com.sinnerschrader.smaller.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private Server server;

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext context) throws Exception {
    server = new Server(context.getProperty("smaller.addr"),
        context.getProperty("smaller.port"));
    server.start();
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    server.stop();
  }

}
