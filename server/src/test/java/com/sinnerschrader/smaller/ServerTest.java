package com.sinnerschrader.smaller;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.junit.Assert.*;

import static org.hamcrest.CoreMatchers.*;

/**
 * @author marwol
 */
public class ServerTest {

  /**
   * @throws Exception
   */
  @Test
  public void testClosure() throws Exception {
    ServerRunnable server = new ServerRunnable();
    try {
      new Thread(new ServerRunnable()).start();
      try {
        Thread.sleep(1500);
      } catch (InterruptedException e) {
      }

      HttpClient client = new HttpClient();
      PostMethod post = new PostMethod("http://localhost:1148");
      try {
        File temp = File.createTempFile("smaller-test-", ".zip");
        assertTrue(temp.delete());
        File target = File.createTempFile("smaller-test-", ".dir");
        assertTrue(target.delete());
        assertTrue(target.mkdir());
        try {
          post.setRequestEntity(new FileRequestEntity(FileUtils.toFile(getClass().getResource("/closure.zip")), "application/zip"));

          int statusCode = client.executeMethod(post);
          assertThat(statusCode, is(HttpStatus.SC_OK));

          InputStream responseBody = post.getResponseBodyAsStream();
          FileUtils.writeByteArrayToFile(temp, IOUtils.toByteArray(responseBody));
          new ZipHandler().unzip(temp, target);

          String basicMin = FileUtils.readFileToString(new File(target, "basic-min.js"));
          assertThat(basicMin, is("(function(){alert(\"Test1\")})();(function(){alert(\"Test 2\")})();"));
        } finally {
          FileUtils.deleteDirectory(target);
          temp.delete();
        }
      } finally {
        post.releaseConnection();
      }
    } finally {
      server.stop();
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

}
