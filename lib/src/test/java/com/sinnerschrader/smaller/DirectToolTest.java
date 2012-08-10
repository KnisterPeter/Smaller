package com.sinnerschrader.smaller;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.sinnerschrader.smaller.lib.ProcessorChain;
import com.sinnerschrader.smaller.lib.Result;
import com.sinnerschrader.smaller.resource.RelativeFileResourceResolver;
import com.sinnerschrader.smaller.resource.impl.JavaEEProcessorFactory;

/**
 * @author markusw
 */
public class DirectToolTest extends AbstractToolTest {

  protected void runToolChain(final String file,
      final ToolChainCallback callback) throws Exception {
    System.out.println("\nRun test: " + file);
    final File target = File.createTempFile("smaller-test-", ".dir");
    assertTrue(target.delete());
    assertTrue(target.mkdir());
    try {
      File source = FileUtils.toFile(this.getClass().getResource("/" + file));
      ProcessorChain chain = new ProcessorChain(new JavaEEProcessorFactory());
      Result result = chain.execute(
          new RelativeFileResourceResolver(source.getAbsolutePath()),
          getManifest(source).getNext());
      callback.test(result);
    } finally {
      FileUtils.deleteDirectory(target);
    }
  }

}
