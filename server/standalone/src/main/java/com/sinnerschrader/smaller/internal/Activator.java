package com.sinnerschrader.smaller.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private ServerRunnable runnable;

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext context) throws Exception {
    runnable = new ServerRunnable(context.getProperty("smaller.addr"),
        context.getProperty("smaller.port"));
    Thread thread = new Thread(runnable);
    thread.setDaemon(true);
    thread.start();
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    runnable.stop();
  }

  private static class ServerRunnable implements Runnable {

    private String host;

    private String port;

    private Server server;

    public ServerRunnable(String host, String port) {
      this.host = host;
      this.port = port;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
      server = new Server(host, port);
      server.start();
    }

    void stop() {
      server.stop();
    }

  }

}
