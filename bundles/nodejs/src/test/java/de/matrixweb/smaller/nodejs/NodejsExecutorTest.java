package de.matrixweb.smaller.nodejs;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import de.matrixweb.vfs.VFS;
import de.matrixweb.vfs.VFSUtils;

import static org.junit.Assert.*;

import static org.hamcrest.CoreMatchers.*;

/**
 * @author marwol
 */
public class NodejsExecutorTest {

  /**
   * @throws IOException
   */
  @Test
  public void test() throws IOException {
    NodeJsExecutor exec = new NodeJsExecutor();
    try {
      VFS vfs = new VFS();
      try {
        VFSUtils.write(vfs.find("/some.file"), "content");

        Map<String, String> options = Collections.emptyMap();
        String path = exec.run(vfs, "/some.file", options);

        assertThat(path, is(nullValue()));
      } finally {
        vfs.dispose();
      }
    } finally {
      exec.dispose();
    }
  }

}
