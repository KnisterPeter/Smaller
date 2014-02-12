package de.matrixweb.smaller.clients.common;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import de.matrixweb.smaller.config.ConfigFile;
import de.matrixweb.smaller.config.Environment;
import de.matrixweb.smaller.config.Processor;

/**
 * @author marwol
 */
public class UtilTest {

  /**
   * @throws Exception
   */
  @Test
  public void testZipAndUnzip() throws Exception {
    final Util util = new Util(new Logger() {
      public void debug(final String message) {
        System.err.println("[DEBUG] " + message);
      }
    });

    final File tempIn = File.createTempFile("smaller", ".dir");
    tempIn.delete();
    tempIn.mkdir();
    final File tempOut = File.createTempFile("smaller", ".dir");
    tempOut.delete();
    tempOut.mkdir();

    new File(tempIn, "dir/dir").mkdirs();
    new File(tempIn, "a.test").createNewFile();
    new File(tempIn, "dir/b.test").createNewFile();
    new File(tempIn, "dir/dir/c.test").createNewFile();
    new File(tempIn, "in").createNewFile();

    final Processor processor = new Processor();
    processor.setSrc("in");
    final ConfigFile configFile = new ConfigFile();
    final Environment env = configFile.getEnvironments().get("first");
    env.setPipeline(new String[] { "processor" });
    env.getProcessors().put("processor", processor);
    env.setProcess("out");

    final byte[] bytes = util.zip(tempIn,
        Arrays.asList("a.test", "dir/b.test", "dir/dir/c.test"), configFile);
    util.unzip(tempOut, bytes);

    assertThat(new File(tempOut, "a.test").exists(), is(true));
    assertThat(new File(tempOut, "dir/b.test").exists(), is(true));
    assertThat(new File(tempOut, "dir/dir/c.test").exists(), is(true));
    assertThat(new File(tempOut, "in").exists(), is(true));

    FileUtils.deleteDirectory(tempIn);
    FileUtils.deleteDirectory(tempOut);
  }

}
