package de.matrixweb.smaller;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;

import de.matrixweb.smaller.common.Manifest;
import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.common.Task;
import de.matrixweb.smaller.pipeline.Result;
import de.matrixweb.smaller.resource.Resources;
import de.matrixweb.smaller.resource.StringResource;
import de.matrixweb.smaller.resource.Type;

/**
 * @author marwol
 */
public abstract class AbstractBaseTest {

  protected abstract void runToolChain(final String file,
      final ToolChainCallback callback) throws Exception;

  protected Manifest getManifest(final File sourceDir) throws IOException {
    return new ObjectMapper().readValue(getMainFile(sourceDir), Manifest.class);
  }

  private File getMainFile(final File input) {
    File main = new File(input, "META-INF/MAIN.json");
    if (!main.exists()) {
      // Old behaviour: Search directly in root of zip
      main = new File(input, "MAIN.json");
      if (!main.exists()) {
        throw new SmallerException(
            "Missing instructions file 'META-INF/MAIN.json'");
      }
    }
    return main;
  }

  protected static void assertOutput(final String result, final String expected) {
    System.out.println("Expected: " + expected.replace("\n", "\\n"));
    System.out.println("Result:   " + result.replace("\n", "\\n"));
    assertThat(result, is(expected));
  }

  protected Result mapResult(final File dir, final Task task)
      throws IOException {
    File js = null;
    File css = null;
    File svg = null;
    final String[] outs = task.getOut();
    for (final String out : outs) {
      if (out.endsWith("js")) {
        js = new File(dir, out);
      } else if (out.endsWith("css")) {
        css = new File(dir, out);
      } else if (out.endsWith("svg")) {
        svg = new File(dir, out);
      }
    }
    final Resources resources = new Resources();
    if (js != null) {
      resources.addResource(new StringResource(null, Type.JS, js
          .getAbsolutePath(), FileUtils.readFileToString(js)));
    }
    if (css != null) {
      resources.addResource(new StringResource(null, Type.CSS, css
          .getAbsolutePath(), FileUtils.readFileToString(css)));
    }
    if (svg != null) {
      resources.addResource(new StringResource(null, Type.SVG, svg
          .getAbsolutePath(), FileUtils.readFileToString(svg)));
    }
    return new Result(resources);
  }

  protected interface ToolChainCallback {

    void test(Result result) throws Exception;

  }

}
