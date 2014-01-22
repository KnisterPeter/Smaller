package de.matrixweb.smaller;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.io.FileUtils;

import de.matrixweb.smaller.common.Manifest;
import de.matrixweb.smaller.common.Version;
import de.matrixweb.smaller.pipeline.Pipeline;
import de.matrixweb.smaller.resource.ProcessorFactory;
import de.matrixweb.smaller.resource.ResourceResolver;
import de.matrixweb.smaller.resource.VFSResourceResolver;
import de.matrixweb.smaller.resource.impl.JavaEEProcessorFactory;
import de.matrixweb.vfs.VFS;
import de.matrixweb.vfs.wrapped.JavaFile;

/**
 * @author markusw
 */
public class DirectToolTest extends AbstractToolTest {

  @Override
  protected void runToolChain(final Version minimum, final String file,
      final ToolChainCallback callback) throws Exception {
    if (Version.getCurrentVersion().isAtLeast(minimum)) {
      System.out.println("\nRun test: " + file);
      final File target = File.createTempFile("smaller-test-", ".dir");
      final ProcessorFactory processorFactory = new JavaEEProcessorFactory();
      try {
        assertTrue(target.delete());
        assertTrue(target.mkdir());
        final File source = FileUtils.toFile(this.getClass().getResource(
            "/" + file));
        final VFS vfs = new VFS();
        try {
          vfs.mount(vfs.find("/"), new JavaFile(source));
          final ResourceResolver resolver = new VFSResourceResolver(vfs);
          final Manifest manifest = getManifest(source);
          new Pipeline(processorFactory).execute(Version.getCurrentVersion(),
              vfs, resolver, manifest, target);
          callback.test(vfs, manifest);
        } finally {
          vfs.dispose();
        }
      } finally {
        processorFactory.dispose();
        FileUtils.deleteDirectory(target);
      }
    }
  }

}
