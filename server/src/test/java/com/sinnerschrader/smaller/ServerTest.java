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
    runToolChain("coffeeScript", new ToolChainCallback() {
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
    runToolChain("closure", new ToolChainCallback() {
      public void test(File directory) throws Exception {
        String basicMin = FileUtils.readFileToString(new File(directory, "basic-min.js"));
        assertThat(basicMin, is("(function(){alert(\"Test1\")})()(function(){alert(\"Test 2\")})();"));
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testUglifyJs() throws Exception {
    runToolChain("uglify", new ToolChainCallback() {
      public void test(File directory) throws Exception {
        String basicMin = FileUtils.readFileToString(new File(directory, "basic-min.js"));
        assertThat(basicMin, is("(function(){alert(\"Test1\")})()(function(){var aLongVariableName=\"Test 2\";alert(aLongVariableName)})()"));
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testClosureUglify() throws Exception {
    runToolChain("closure-uglify", new ToolChainCallback() {
      public void test(File directory) throws Exception {
        String basicMin = FileUtils.readFileToString(new File(directory, "basic-min.js"));
        assertThat(basicMin, is("(function(){alert(\"Test1\")})()(function(){alert(\"Test 2\")})()"));
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testLessJs() throws Exception {
    runToolChain("lessjs", new ToolChainCallback() {
      public void test(File directory) throws Exception {
        String css = FileUtils.readFileToString(new File(directory, "style.css"));
        assertThat(css, is("#header {\n  color: #4d926f;\n}\nh2 {\n  color: #4d926f;\n}\n.background {\n  background: url('some/where.png');\n}\n"));
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testLessJsIncludes() throws Exception {
    runToolChain("lessjs-includes", new ToolChainCallback() {
      public void test(File directory) throws Exception {
        String css = FileUtils.readFileToString(new File(directory, "style.css"));
        assertThat(css, is("#header {\n  color: #4d926f;\n}\nh2 {\n  color: #4d926f;\n}\n.background {\n  background: url('../some/where.png');\n}\n"));
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
    runToolChain("any", new ToolChainCallback() {
      public void test(File directory) throws Exception {
        String basicMin = FileUtils.readFileToString(new File(directory, "basic-min.js"));
        assertThat(basicMin, is("(function(){alert(\"Test1\")})()(function(){alert(\"Test 2\")})()"));
        String css = FileUtils.readFileToString(new File(directory, "style.css"));
        assertThat(css, is("#header{color:#4d926f}h2{color:#4d926f;background-image:url(data:image/gif;base64,R0lGODlhZABkAP"
            + "AAAERERP///ywAAAAAZABkAEAI/wABCBxIsKDBgwgTKlzIsKHDhxAjSpxIsaLFixgzatzIMYBHjxxDPvwI0iHJkyhLJkx5EiLLlAZfs"
            + "lxJ0uTHkTULtrS402XOhT1FHkSJMKhPmUVlvhyqFKbCpkKjSp1KtarVq1izat3KtavXr2DDihVrdGzEsgzRJv3J9GZbt0DXqsQJ92l"
            + "TqHKXAmVrs65dvlTRqjVLuLDhw4gTK17MuLHjx5AjS55M" + "ubLlwY8x57UsUDPBu4A7g/arc7Rf0wFokt6cNrTnvathz1WN"
            + "WjDRia9Lx9Y9O6RS2qnPhn4bHKvt3X9751Wuu23r4bmXIwcAWjbevrc5a9/Ovbv37+DDi7wfT768+fPo06tfz769+/fw48ufT19r9MT3RU9vnJ/6c"
            + "Mj99feZUxIhZR1d1UmnV3IJDoRaTKYR1xuBEKrF14OsNeTZccwhWJyH2H3IYIUd"
            + "hljgfwPu5yBg2eGGYoYM1qbcbyAKp6KMLZJoIIwmPldiRxSm+CNxGgpYUY76Daljj8a59iKRIYr41FA18iZlkTRauRqS/jk5"
            + "2F0+Bilkg1peF6ORNgY4U31stunmm3DGKeecdNZp55145plVQAA7)}"));
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testCssEmbed() throws Exception {
    runToolChain("cssembed", new ToolChainCallback() {
      public void test(File directory) throws Exception {
        String css = FileUtils.readFileToString(new File(directory, "css/style-base64.css"));
        assertThat(css, is(".background {\n  background: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGQAAABkCAIAAAD/gAIDAA"
            + "ABaElEQVR42u3aQRKCMAwAQB7n/7+EVx1HbZsEStlcldAsUJrq9hDNsSGABQsWLFiwEMCCBQsWLFgIYMGCBQsWLASwYMGCBQsWAliwYMGCBQtB"
            + "CGt/j1UrHygTFixYsGDBgnUE1v4lKnK2Zw5h7f0RLGmMLDjCSJlVWOnuYyP8zDwdVkpVKTlnx4o8Dr1YLV8uwUqZ4IP3S1ba1wPnfRsWvZUqVj"
            + "MnYw1ffFiZBy6OlTvu1bBKZ7rc1f90WJGILx3ujpXSD94Iq/0ssLpPtOYEX7RR03WTro8V2TW7NVbvImOuOWtyr6u2O6fsr8O6LNY8T+JxWEd6"
            + "/SisGqvlFFvpZsvAenrg0+HBl2DFO97g5S1qthP24NsTVbQ+uQlTurT/WLnNxIS/rQ2UuUVyJXbX6Y16YpvZgXVK41Z3/SLhD7iwYMGCBUvAgg"
            + "ULFixYAhYsWLBgwRKwYMGCBQuWgAULFixYsAQsWDXxBFVy4xyOC7MdAAAAAElFTkSuQmCC);\n}\n"));
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testOutputOnly() throws Exception {
    runToolChain("out-only", new ToolChainCallback() {
      public void test(File directory) throws Exception {
        assertThat(directory.list().length, is(2));
        assertThat(new File(directory, "basic-min.js").exists(), is(true));
        assertThat(new File(directory, "style.css").exists(), is(true));
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  @Ignore
  public void testClosureError() throws Exception {
    runToolChain("closure-error", new ToolChainCallback() {
      public void test(File directory) throws Exception {
        String basicMin = FileUtils.readFileToString(new File(directory, "basic-min.js"));
        assertThat(basicMin, is("(function(){alert(\"Test1\")})()(function(){alert(\"Test 2\")})();"));
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testUnicodeEscape() throws Exception {
    runToolChain("unicode-escape", new ToolChainCallback() {
      public void test(File directory) throws Exception {
        String basicMin = FileUtils.readFileToString(new File(directory, "basic-min.js"));
        System.out.println(basicMin);
        System.out
            .println("var stringEscapes={\"\\\\\":\"\\\\\",\"'\":\"'\",\"\\n\":\"n\",\"\\r\":\"r\",\"\t\":\"t\",\"\\u2028\":\"u2028\",\"\\u2029\":\"u2029\"}");
        assertThat(basicMin,
            is("var stringEscapes={\"\\\\\":\"\\\\\",\"'\":\"'\",\"\\n\":\"n\",\"\\r\":\"r\",\"\t\":\"t\",\"\\u2028\":\"u2028\",\"\\u2029\":\"u2029\"}"));
      }
    });
  }

}
