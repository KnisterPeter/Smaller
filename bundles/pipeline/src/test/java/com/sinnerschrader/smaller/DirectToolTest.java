package com.sinnerschrader.smaller;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.sinnerschrader.smaller.pipeline.Pipeline;
import com.sinnerschrader.smaller.pipeline.Result;
import com.sinnerschrader.smaller.resource.FileResourceResolver;
import com.sinnerschrader.smaller.resource.impl.JavaEEProcessorFactory;

/**
 * @author markusw
 */
public class DirectToolTest extends AbstractToolTest {

  @Override
  protected void runToolChain(final String file,
      final ToolChainCallback callback) throws Exception {
    System.out.println("\nRun test: " + file);
    final File target = File.createTempFile("smaller-test-", ".dir");
    assertTrue(target.delete());
    assertTrue(target.mkdir());
    try {
      final File source = FileUtils.toFile(this.getClass().getResource(
          "/" + file));
      final Pipeline chain = new Pipeline(
          new JavaEEProcessorFactory());
      final Result result = chain.execute(
          new FileResourceResolver(source.getAbsolutePath()),
          getManifest(source).getNext());
      callback.test(result);
    } finally {
      FileUtils.deleteDirectory(target);
    }
  }

}
