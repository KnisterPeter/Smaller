package com.sinnerschrader.smaller;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;

import com.sinnerschrader.smaller.common.Manifest;
import com.sinnerschrader.smaller.common.SmallerException;
import com.sinnerschrader.smaller.lib.ProcessorChain;
import com.sinnerschrader.smaller.lib.RequestContext;

import static org.junit.Assert.*;

import static org.hamcrest.CoreMatchers.*;

/**
 * @author marwol
 */
public abstract class AbstractBaseTest {

  protected void runToolChain(final String file, final ToolChainCallback callback) throws Exception {
    System.out.println("\nRun test: " + file);
    final File target = File.createTempFile("smaller-test-", ".dir");
    assertTrue(target.delete());
    assertTrue(target.mkdir());
    try {
      File source = FileUtils.toFile(this.getClass().getResource("/" + file));
      RequestContext requestContext = setUpContext(source, target);
      ProcessorChain chain = new ProcessorChain();
      chain.execute(requestContext);
      callback.test(target);
    } finally {
      FileUtils.deleteDirectory(target);
    }
  }

  private RequestContext setUpContext(final File source, final File target) throws IOException {
    final RequestContext context = new RequestContext();
    context.setInput(source);
    context.setOutput(target);
    final Manifest manifest = new ObjectMapper().readValue(getMainFile(context.getInput()), Manifest.class);
    context.setManifest(manifest);
    return context;
  }

  private File getMainFile(final File input) {
    File main = new File(input, "META-INF/MAIN.json");
    if (!main.exists()) {
      // Old behaviour: Search directly in root of zip
      main = new File(input, "MAIN.json");
      if (!main.exists()) {
        throw new SmallerException("Missing instructions file 'META-INF/MAIN.json'");
      }
    }
    return main;
  }

  protected static void assertOutput(final String result, final String expected) {
    System.out.println("Expected: " + expected.replace("\n", "\\n"));
    System.out.println("Result:   " + result.replace("\n", "\\n"));
    assertThat(result, is(expected));
  }

  protected interface ToolChainCallback {

    void test(File directory) throws Exception;

  }

}
