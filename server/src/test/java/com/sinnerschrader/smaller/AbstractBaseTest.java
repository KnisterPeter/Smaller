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

import com.sinnerschrader.smaller.common.Zip;

import static org.junit.Assert.*;

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
    boolean createZip = false;
    final File temp = File.createTempFile("smaller-test-", ".zip");
    assertTrue(temp.delete());
    final File target = File.createTempFile("smaller-test-", ".dir");
    assertTrue(target.delete());
    assertTrue(target.mkdir());
    File zip = FileUtils.toFile(getClass().getResource("/" + file));
    try {
      if (zip.isDirectory()) {
        createZip = true;
        File out = File.createTempFile("temp-", ".zip");
        out.delete();
        Zip.zip(new FileOutputStream(out), zip);
        zip = out;
      }
      uploadZipFile(zip, temp, new Callback() {
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

  private void uploadZipFile(File zip, File target, Callback callback) throws Exception {
    HttpResponse response = Request.Post("http://localhost:1148").body(new FileEntity(zip, ContentType.create("application/zip"))).execute().returnResponse();
    InputStream in = response.getEntity().getContent();
    try {
      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        throw new RuntimeException(IOUtils.toString(in));
      }
      FileUtils.writeByteArrayToFile(target, IOUtils.toByteArray(in));
      callback.execute();
    } finally {
      IOUtils.closeQuietly(in);
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
