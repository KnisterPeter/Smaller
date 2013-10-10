package de.matrixweb.smaller.nodejs;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.ResourceResolver;
import de.matrixweb.smaller.resource.StringResource;

/**
 * @author marwol
 */
public class NodejsExecutorTest {

  /**
   * @throws IOException
   */
  @Test
  public void test() throws IOException {
    ResourceResolver resolver = new ResourceResolver() {
      @Override
      public File writeAll() throws IOException {
        File file = File.createTempFile("smaller-node-test", ".dir");
        file.delete();
        file.mkdirs();
        return file;
      }

      @Override
      public Resource resolve(final String path) {
        return null;
      }
    };

    NodejsExecutor exec = new NodejsExecutor();
    try {
      Map<String, String> options = Collections.emptyMap();
      exec.run(new StringResource(resolver, null, "some.res", "content"), options);
    } finally {
      exec.dispose();
    }
  }

}
