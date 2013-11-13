package de.matrixweb.smaller;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import de.matrixweb.smaller.clients.common.Logger;
import de.matrixweb.smaller.clients.common.Util;
import de.matrixweb.smaller.common.Manifest;
import de.matrixweb.smaller.common.Version;
import de.matrixweb.smaller.common.Zip;
import de.matrixweb.smaller.internal.Server;

/**
 * @author markusw
 */
public class StandaloneToolTest extends AbstractToolTest {

  private static ServerRunnable serverRunnable;

  private final Util util = new Util(new Logger() {
    @Override
    public void debug(final String message) {
      System.out.println(message);
    }
  });

  /** */
  @BeforeClass
  public static void startServer() {
    serverRunnable = new ServerRunnable();
    new Thread(serverRunnable).start();
    try {
      Thread.sleep(1500);
    } catch (final InterruptedException e) {
    }
  }

  /** */
  @AfterClass
  public static void stopServer() {
    serverRunnable.stop();
  }

  /**
   * @see de.matrixweb.smaller.AbstractBaseTest#runToolChain(java.lang.String,
   *      de.matrixweb.smaller.AbstractBaseTest.ToolChainCallback)
   */
  @Override
  protected void runToolChain(final Version minimum, final String file,
      final ToolChainCallback callback) throws Exception {
    if (Version.getCurrentVersion().isAtLeast(minimum)) {
      System.out.println("\nExecuting: " + file);
      prepareTestFiles(file, callback, new ExecuteTestCallback() {
        @Override
        public void execute(final Manifest manifest, final File source,
            final File target) throws Exception {
          final ByteArrayOutputStream baos = new ByteArrayOutputStream();
          Zip.zip(baos, source);
          final byte[] bytes = StandaloneToolTest.this.util.send("127.0.0.1",
              "1148", baos.toByteArray());
          final File zip = File.createTempFile(
              "smaller-standalone-test-response", "zip");
          try {
            FileUtils.writeByteArrayToFile(zip, bytes);
            Zip.unzip(zip, target);
          } finally {
            zip.delete();
          }
        }
      });
    }
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
