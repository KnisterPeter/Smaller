package com.sinnerschrader.smaller;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
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
  public void testCoffeeScript() throws Exception {
    runToolChain("coffeeScript.zip", new ToolChainCallback() {
      public void test(File directory) throws Exception {
        String basicMin = FileUtils.readFileToString(new File(directory, "script.js"));
        assertThat(basicMin, is("(function() {\n  var square;\n\n  square = function(x) {\n    return x * x;\n  };\n\n}).call(this);\n"));
      }
    });
  }

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
  public void testUglifyJs() throws Exception {
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

  /**
   * @throws Exception
   */
  @Test
  public void testLessJs() throws Exception {
    runToolChain("lessjs.zip", new ToolChainCallback() {
      public void test(File directory) throws Exception {
        String css = FileUtils.readFileToString(new File(directory, "style.css"));
        assertThat(css, is("#header {\n  color: #4d926f;\n}\nh2 {\n  color: #4d926f;\n}\n"));
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  @Ignore("Currently sass does not work as expected")
  public void testSass() throws Exception {
    runToolChain("sass.zip", new ToolChainCallback() {
      public void test(File directory) throws Exception {
        String css = FileUtils.readFileToString(new File(directory, "style.css"));
        assertThat(css, is(""));
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testAny() throws Exception {
    runToolChain("any.zip", new ToolChainCallback() {
      public void test(File directory) throws Exception {
        String basicMin = FileUtils.readFileToString(new File(directory, "basic-min.js"));
        assertThat(basicMin, is("(function(){alert(\"Test1\")})()(function(){alert(\"Test 2\")})()"));
        String css = FileUtils.readFileToString(new File(directory, "style.css"));
        assertThat(css, is("#header{color:#4d926f}h2{color:#4d926f;background-image:url(data:image/gif;base64,R0lGODlhZABkAPAAAERERP///ywAAAAA"
            + "ZABkAEAI/wABCBxIsKDBgwgTKlzIsKHDhxAjSpxIsaLFixgzatzIMYBHjxxDPvwI0iHJkyhLJkx5EiLLlAZfslxJ0uTHkTULtrS402XOhT1FHkS"
            + "JMKhPmUVlvhyqFKbCpkKjSp1KtarVq1izat3KtavXr2DDihVrdGzEsgzRJv3J9GZbt0DXqsQJ92lTqHKXAmVrs65dvlTRqjVLuLDhw4gTK17MuL"
            + "Hjx5AjS55MubLlwY8x57UsUDPBu4A7g/arc7Rf0wFokt6cNrTnvathz1WNWjDRia9Lx9Y9O6RS2qnPhn4bHKvt3X9751Wuu23r3bmXIwcAWjbev"
            + "rc5a9/Ovbv37+DDi74fT768+fPo06tfz769+/fw48ufT19r9MT3RU9vnJ/6cMj99feZUxIhZR1d1UmnV3IJDoRaTKYR1xuBEKrF14OsNeTZccwh"
            + "WJyH2H3IYIUdhljgfwPu5yBg2eGGYoYM1sacgSYKp6KMLZJII3En3vjiRjPxaGOJK/7YEYciamikfyoeuVqAueX41FAg6pjkczsWieSVUjL5311"
            + "YUshbg2OCGWaXIgV5FJr1tenmm3DGKeecdNZp55145qmnVAEBADs=)}"));
      }
    });
  }

}
