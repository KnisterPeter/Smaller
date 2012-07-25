package com.sinnerschrader.smaller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.sinnerschrader.smaller.common.SmallerException;
import com.sinnerschrader.smaller.common.Zip;

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
    } catch (final InterruptedException e) {
    }
  }

  /** */
  @AfterClass
  public static void stopServer() {
    serverRunnable.stop();
  }

  protected void runToolChain(final String file, final ToolChainCallback callback) throws Exception {
    boolean createZip = false;
    final File temp = File.createTempFile("smaller-test-", ".zip");
    assertTrue(temp.delete());
    final File target = File.createTempFile("smaller-test-", ".dir");
    assertTrue(target.delete());
    assertTrue(target.mkdir());
    File zip = FileUtils.toFile(this.getClass().getResource("/" + file));
    try {
      if (zip.isDirectory()) {
        createZip = true;
        final File out = File.createTempFile("temp-", ".zip");
        out.delete();
        Zip.zip(new FileOutputStream(out), zip);
        zip = out;
      }
      this.uploadZipFile(zip, temp, new Callback() {
        @Override
        public void execute() throws Exception {
          Zip.unzip(temp, target);
          callback.test(target);
        }
      });
    } finally {
      FileUtils.deleteDirectory(target);
      temp.delete();
      if (createZip) {
        zip.delete();
      }
    }
  }

  private void uploadZipFile(final File zip, final File target, final Callback callback) throws Exception {
    final HttpResponse response = Request.Post("http://localhost:1148").body(new FileEntity(zip, ContentType.create("application/zip"))).execute()
        .returnResponse();
    final InputStream in = response.getEntity().getContent();
    try {
      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        throw new RuntimeException(IOUtils.toString(in));
      }
      if (!this.getHeader(response, "X-Smaller-Status").equals("OK")) {
        throw new SmallerException(this.getHeader(response, "X-Smaller-Message"));
      }
      FileUtils.writeByteArrayToFile(target, IOUtils.toByteArray(in));
      callback.execute();
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  private String getHeader(final HttpResponse response, final String name) {
    return response.getFirstHeader(name).getValue();
  }

  protected static void assertOutput(final String result, final String expected) {
    System.out.println("Expected: " + expected);
    System.out.println("Result:   " + result);
    assertThat(result, is(expected));
  }

  private static class ServerRunnable implements Runnable {

    private final Server server;

    public ServerRunnable() {
      server = new Server();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
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
