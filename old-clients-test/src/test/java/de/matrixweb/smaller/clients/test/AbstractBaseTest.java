package de.matrixweb.smaller.clients.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import de.matrixweb.smaller.AbstractToolTest;
import de.matrixweb.smaller.internal.Server;

/**
 * @author marwol
 */
public abstract class AbstractBaseTest extends AbstractToolTest {

  private static ServerRunnable serverRunnable;

  /**
   * 
   */
  @BeforeClass
  public static void startServer() {
    serverRunnable = new ServerRunnable();
    new Thread(serverRunnable).start();
    try {
      Thread.sleep(1500);
    } catch (final InterruptedException e) {
    }
  }

  /**
   * 
   */
  @AfterClass
  public static void stopServer() {
    serverRunnable.stop();
  }

  private static class ServerRunnable implements Runnable {

    private final Server server;

    public ServerRunnable() {
      this.server = new Server();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
      this.server.start();
    }

    public void stop() {
      this.server.stop();
    }

  }

}
