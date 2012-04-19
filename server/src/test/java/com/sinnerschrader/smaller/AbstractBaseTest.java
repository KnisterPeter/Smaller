package com.sinnerschrader.smaller;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import static org.junit.Assert.*;

import static org.hamcrest.CoreMatchers.*;

/**
 * @author marwol
 */
public abstract class AbstractBaseTest {

  private static ServerRunnable serverRunnable;

  /** */
  @BeforeClass
  public static void startServer() {
    serverRunnable = new ServerRunnable();
    new Thread(new ServerRunnable()).start();
    try {
      Thread.sleep(1500);
    } catch (InterruptedException e) {
    }
  }

  /** */
  @AfterClass
  public static void stopServer() {
    serverRunnable.stop();
  }

  protected void runToolChain(final String file, final ToolChainCallback callback) throws Exception {
    final File temp = File.createTempFile("smaller-test-", ".zip");
    assertTrue(temp.delete());
    final File target = File.createTempFile("smaller-test-", ".dir");
    assertTrue(target.delete());
    assertTrue(target.mkdir());
    try {
      uploadZipFile(file, temp, new Callback() {
        public void execute() throws Exception {
          new ZipHandler().unzip(temp, target);
          callback.test(target);
        }
      });
    } finally {
      FileUtils.deleteDirectory(target);
      temp.delete();
    }
  }

  private void uploadZipFile(String name, File response, Callback callback) throws Exception {
    HttpClient client = new HttpClient();
    PostMethod post = new PostMethod("http://localhost:1148");
    try {
      post.setRequestEntity(new FileRequestEntity(FileUtils.toFile(getClass().getResource("/" + name)), "application/zip"));
      int statusCode = client.executeMethod(post);
      assertThat(statusCode, is(HttpStatus.SC_OK));
      InputStream responseBody = post.getResponseBodyAsStream();
      FileUtils.writeByteArrayToFile(response, IOUtils.toByteArray(responseBody));
      callback.execute();
    } finally {
      post.releaseConnection();
    }
  }

  private static class ServerRunnable implements Runnable {

    private Server server;

    public ServerRunnable() {
      server = new Server();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
      server.start(new String[] {});
    }

    public void stop() {
      server.stop();
    }

  }

  private interface Callback {

    void execute() throws Exception;

  }

  protected interface ToolChainCallback {

    void test(File directory) throws Exception;

  }

}
