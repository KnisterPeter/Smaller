package com.sinnerschrader.smaller;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import static org.junit.Assert.*;

import static org.hamcrest.CoreMatchers.*;

/**
 * @author marwol
 */
public class ServerTest extends AbstractBaseTest {

  /**
   * @throws Exception
   */
  @Test
  public void testClosure() throws Exception {
    runToolChain("closure.zip", new ToolChainCallback() {
      public void test(File directory) throws Exception {
        String basicMin = FileUtils.readFileToString(new File(directory, "basic-min.js"));
        assertThat(basicMin, is("(function(){alert(\"Test1\")})();(function(){alert(\"Test 2\")})();"));
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testUglify() throws Exception {
    runToolChain("uglify.zip", new ToolChainCallback() {
      public void test(File directory) throws Exception {
        String basicMin = FileUtils.readFileToString(new File(directory, "basic-min.js"));
        assertThat(basicMin, is("(function(){alert(\"Test1\")})()(function(){var a=\"Test 2\";alert(a)})()"));
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testClosureUglify() throws Exception {
    runToolChain("closure-uglify.zip", new ToolChainCallback() {
      public void test(File directory) throws Exception {
        String basicMin = FileUtils.readFileToString(new File(directory, "basic-min.js"));
        assertThat(basicMin, is("(function(){alert(\"Test1\")})(),function(){alert(\"Test 2\")}()"));
      }
    });
  }

}
