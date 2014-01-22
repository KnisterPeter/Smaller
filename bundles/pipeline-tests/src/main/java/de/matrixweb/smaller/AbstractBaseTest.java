package de.matrixweb.smaller;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import de.matrixweb.smaller.common.Manifest;
import de.matrixweb.smaller.common.Version;
import de.matrixweb.smaller.common.Zip;
import de.matrixweb.vfs.VFS;
import de.matrixweb.vfs.wrapped.JavaFile;

/**
 * @author marwol
 */
public abstract class AbstractBaseTest {

  protected abstract void runToolChain(Version minimum, final String file,
      final ToolChainCallback callback) throws Exception;

  protected Manifest getManifest(final File sourceDir) throws IOException {
    return new ObjectMapper().readValue(getMainFile(sourceDir), Manifest.class);
  }

  private File getMainFile(final File input) {
    return new File(input, "test.setup");
  }

  protected void copyManifest(final File input) throws IOException {
    final File main = getMainFile(input);
    final File target = new File(input, "META-INF/smaller.json");
    if (!target.exists()) {
      FileUtils.moveFile(main, target);
    }
  }

  protected static void assertOutput(final String result, final String expected) {
    try {
      assertThat(result, is(expected));
    } catch (final AssertionError e) {
      System.err.println("Expected: " + expected.replace("\n", "\\n"));
      System.err.println("Result:   " + result.replace("\n", "\\n"));
      final int len = StringUtils.difference(expected, result).length();
      if (len > 0) {
        System.err.println("          "
            + StringUtils.repeat('-', result.length() - len + 1) + '^');
      }
      throw e;
    }
  }

  protected void prepareTestFiles(final String file,
      final ToolChainCallback testCallback,
      final ExecuteTestCallback executeCallback) throws Exception {
    final Enumeration<URL> urls = getClass().getClassLoader()
        .getResources(file);
    if (!urls.hasMoreElements()) {
      fail(String.format("Test sources '%s' not found", file));
    }
    final File testTempSource = File.createTempFile("smaller-test-input",
        ".dir");
    try {
      testTempSource.delete();
      testTempSource.mkdirs();

      URL url = null;
      while (urls.hasMoreElements()
          && (url == null || !url.toString().contains("/test-classes/"))) {
        url = urls.nextElement();
      }
      File source = null;
      if ("jar".equals(url.getProtocol())) {
        final int idx = url.getFile().indexOf('!');
        final String jar = url.getFile().substring(5, idx);
        final String entryPath = url.getFile().substring(idx + 1);
        Zip.unzip(new File(jar), testTempSource);
        source = new File(testTempSource, entryPath);
      } else {
        FileUtils
            .copyDirectory(new File(url.toURI().getPath()), testTempSource);
        source = testTempSource;
      }
      final File target = File.createTempFile("smaller-test", ".dir");
      try {
        target.delete();
        target.mkdirs();

        // copyManifest(source);
        final Manifest manifest = getManifest(source);
        executeCallback.execute(manifest, source, target);
        final VFS vfs = new VFS();
        try {
          vfs.mount(vfs.find("/"), new JavaFile(target));
          if (testCallback != null) {
            testCallback.test(vfs, manifest);
          }
        } finally {
          vfs.dispose();
        }
      } finally {
        if (target.exists()) {
          FileUtils.deleteDirectory(target);
        }
      }
    } finally {
      if (testTempSource != null) {
        FileUtils.deleteDirectory(testTempSource);
      }
    }
  }

  protected interface ExecuteTestCallback {

    void execute(Manifest manifest, File source, File target) throws Exception;

  }

  protected interface ToolChainCallback {

    void test(VFS vfs, Manifest manifest) throws Exception;

  }

}
