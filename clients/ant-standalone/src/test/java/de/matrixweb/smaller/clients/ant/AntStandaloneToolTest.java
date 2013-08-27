package de.matrixweb.smaller.clients.ant;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.junit.Before;

import com.google.common.base.Joiner;

import de.matrixweb.smaller.AbstractToolTest;
import de.matrixweb.smaller.common.Manifest;
import de.matrixweb.smaller.common.Task;
import de.matrixweb.smaller.common.Zip;

import static org.junit.Assert.*;

/**
 * @author marwol
 */
public class AntStandaloneToolTest extends AbstractToolTest {

  private SmallerTask task;

  /** */
  @Before
  public void setupTask() {
    this.task = new SmallerTask();
  }

  /**
   * @see de.matrixweb.smaller.AbstractBaseTest#runToolChain(java.lang.String,
   *      de.matrixweb.smaller.AbstractBaseTest.ToolChainCallback)
   */
  @Override
  protected void runToolChain(final String file, final ToolChainCallback callback) throws Exception {
    final Enumeration<URL> urls = getClass().getClassLoader().getResources(file);
    if (!urls.hasMoreElements()) {
      fail(String.format("Test sources '%s' not found", file));
    }
    boolean deleteSource = false;
    File jarContent = null;
    File source = null;
    try {
      URL url = null;
      while (urls.hasMoreElements() && (url == null || !url.toString().contains("/test-classes/"))) {
        url = urls.nextElement();
      }
      if ("jar".equals(url.getProtocol())) {
        final int idx = url.getFile().indexOf('!');
        final String jar = url.getFile().substring(5, idx);
        final String entryPath = url.getFile().substring(idx + 1);
        jarContent = File.createTempFile("smaller-standalone-test-input", ".dir");
        deleteSource = true;
        jarContent.delete();
        jarContent.mkdirs();
        Zip.unzip(new File(jar), jarContent);
        source = new File(jarContent, entryPath);
      } else {
        source = new File(url.toURI().getPath());
      }
      File target = File.createTempFile("ant-standalone-target", ".dir");
      try {
        target.delete();
        target.mkdirs();
        Manifest manifest = getManifest(source);
        Task task = manifest.getNext();
        this.task.setProcessor(task.getProcessor());
        this.task.setIn(Joiner.on(',').join(task.getIn()));
        this.task.setOut(Joiner.on(',').join(task.getOut()));
        this.task.setOptions(task.getOptionsDefinition());
        final File finalSource = source;
        this.task.setFiles(new FileSet() {
          @Override
          public DirectoryScanner getDirectoryScanner() {
            return new DirectoryScanner() {
              @Override
              public synchronized File getBasedir() {
                return finalSource;
              }
            };
          }
        });
        this.task.setTarget(target);
        this.task.execute();
        callback.test(mapResult(target, manifest.getCurrent()));
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

}
