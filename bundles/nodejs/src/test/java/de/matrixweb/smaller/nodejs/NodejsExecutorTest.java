package de.matrixweb.smaller.nodejs;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.VFSResourceResolver;
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
    NodejsExecutor exec = new NodejsExecutor();
    try {
      VFS vfs = new VFS();
      try {
        VFSResourceResolver resolver = new VFSResourceResolver(vfs);
        VFSUtils.write(vfs.find("/some.file"), "content");

        Map<String, String> options = Collections.emptyMap();
        Resource result = exec.run(vfs, resolver.resolve("/some.file"), options);

        assertThat(result.getContents(), is("content"));
      } finally {
        vfs.dispose();
      }
    } finally {
      exec.dispose();
    }
  }

}
