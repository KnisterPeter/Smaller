package de.matrixweb.smaller.javascript;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;

/**
 * @author markusw
 */
public class JavaScriptExecutorTest {

  /**
   * @throws IOException
   */
  @Test
  public void testGlobalFunction() throws IOException {
    final JavaScriptExecutor jse = new JavaScriptExecutor(
        "test-global-function");
    jse.addGlobalFunction("test", new Global());
    jse.addScriptSource(
        "function exec(input) { return new String(test(input)); }", "script");
    jse.addCallScript("exec(%s);");

    final StringWriter output = new StringWriter();
    jse.run(new StringReader("input"), output);
    assertThat(output.toString(), is("result input"));
  }

  /** */
  public static class Global {

    /**
     * @param input
     * @return ...
     */
    public String test(final String input) {
      return "result " + input;
    }

  }

}
