package de.matrixweb.smaller.nodejs;

import java.io.IOException;

import org.junit.Test;

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
      exec.run("command");
    } finally {
      exec.dispose();
    }
  }

}
