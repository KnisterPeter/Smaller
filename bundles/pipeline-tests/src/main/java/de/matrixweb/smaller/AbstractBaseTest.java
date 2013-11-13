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
import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.common.Task;
import de.matrixweb.smaller.common.Version;
import de.matrixweb.smaller.common.Zip;
import de.matrixweb.smaller.pipeline.Result;
import de.matrixweb.smaller.resource.Resources;
import de.matrixweb.smaller.resource.VFSResourceResolver;
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

  protected Result mapResult(final VFS vfs, final Task task) throws IOException {
    final Resources resources = new Resources();
    final VFSResourceResolver resolver = new VFSResourceResolver(vfs);
    for (final String out : task.getOut()) {
      resources.addResource(resolver.resolve('/' + out));
    }
    return new Result(resources);
  }

  protected void prepareTestFiles(final String file,
      final ToolChainCallback testCallback,
      final ExecuteTestCallback executeCallback) throws Exception {
    final Enumeration<URL> urls = getClass().getClassLoader()
        .getResources(file);
    if (!urls.hasMoreElements()) {
      fail(String.format("Test sources '%s' not found", file));
    }
    boolean deleteSource = false;
    File jarContent = null;
    File source = null;
    try {
      URL url = null;
      while (urls.hasMoreElements()
          && (url == null || !url.toString().contains("/test-classes/"))) {
        url = urls.nextElement();
      }
      if ("jar".equals(url.getProtocol())) {
        final int idx = url.getFile().indexOf('!');
        final String jar = url.getFile().substring(5, idx);
        final String entryPath = url.getFile().substring(idx + 1);
        jarContent = File.createTempFile("smaller-standalone-test-input",
            ".dir");
        deleteSource = true;
        jarContent.delete();
        jarContent.mkdirs();
        Zip.unzip(new File(jar), jarContent);
        source = new File(jarContent, entryPath);
      } else {
        source = new File(url.toURI().getPath());
      }
      final File target = File.createTempFile("smaller-test", ".dir");
      try {
        target.delete();
        target.mkdirs();
        final Manifest manifest = getManifest(source);
        manifest.getNext();

        executeCallback.execute(manifest, source, target);

        final VFS vfs = new VFS();
        try {
          vfs.mount(vfs.find("/"), new JavaFile(target));
          testCallback.test(vfs, mapResult(vfs, manifest.getCurrent()));
        } finally {
          vfs.dispose();
        }
      } finally {
        if (target.exists()) {
          FileUtils.deleteDirectory(target);
        }
      }
    } finally {
      if (deleteSource && jarContent != null) {
        FileUtils.deleteDirectory(jarContent);
      }
    }
  }

  protected interface ExecuteTestCallback {

    void execute(Manifest manifest, File source, File target) throws Exception;

  }

  protected interface ToolChainCallback {

    void test(VFS vfs, Result result) throws Exception;

  }

}
