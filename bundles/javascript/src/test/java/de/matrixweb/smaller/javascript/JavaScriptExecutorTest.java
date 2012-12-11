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
  public void testRhinoEngine() throws IOException {
    final JavaScriptExecutor engine = new JavaScriptExecutorRhino(
        "test-global-function");
    testGlobalFunction(engine);
  }

  /**
   * @throws IOException
   */
  @Test
  public void testV8Engine() throws IOException {
    final JavaScriptExecutor engine = new JavaScriptExecutorV8();
    testGlobalFunction(engine);
  }

  private void testGlobalFunction(final JavaScriptExecutor engine)
      throws IOException {
    engine.addGlobalFunction("test", new Global());
    engine.addScriptSource(
        "function exec(input) { return new String(test(input)); }", "script");
    engine.addCallScript("exec(%s);");

    final StringWriter output = new StringWriter();
    engine.run(new StringReader("input"), output);
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
