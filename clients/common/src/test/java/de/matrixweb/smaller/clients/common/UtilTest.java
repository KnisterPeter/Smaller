package de.matrixweb.smaller.clients.common;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import de.matrixweb.smaller.clients.common.Logger;
import de.matrixweb.smaller.clients.common.Util;

import static org.junit.Assert.*;

import static org.hamcrest.CoreMatchers.*;

/**
 * @author marwol
 */
public class UtilTest {

  /**
   * @throws Exception
   */
  @Test
  public void testZipAndUnzip() throws Exception {
    Util util = new Util(new Logger() {
      public void debug(String message) {
        System.err.println("[DEBUG] " + message);
      }
    });

    File tempIn = File.createTempFile("smaller", ".dir");
    tempIn.delete();
    tempIn.mkdir();
    File tempOut = File.createTempFile("smaller", ".dir");
    tempOut.delete();
    tempOut.mkdir();

    new File(tempIn, "dir/dir").mkdirs();
    new File(tempIn, "a.test").createNewFile();
    new File(tempIn, "dir/b.test").createNewFile();
    new File(tempIn, "dir/dir/c.test").createNewFile();
    new File(tempIn, "in").createNewFile();

    byte[] bytes = util.zip(tempIn, new String[] { "a.test", "dir/b.test", "dir/dir/c.test" }, "processor", "in", "out");
    util.unzip(tempOut, bytes);

    assertThat(new File(tempOut, "a.test").exists(), is(true));
    assertThat(new File(tempOut, "dir/b.test").exists(), is(true));
    assertThat(new File(tempOut, "dir/dir/c.test").exists(), is(true));
    assertThat(new File(tempOut, "in").exists(), is(true));

    FileUtils.deleteDirectory(tempIn);
    FileUtils.deleteDirectory(tempOut);
  }

}
