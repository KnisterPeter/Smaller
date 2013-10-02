package de.matrixweb.smaller.nodejs;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

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
    NodejsExecutor exec = new NodejsExecutor();
    try {
      Map<String, String> options = Collections.emptyMap();
      exec.run(new StringResource(null, null, "some.res", "content"), options);
    } finally {
      exec.dispose();
    }
  }

}
