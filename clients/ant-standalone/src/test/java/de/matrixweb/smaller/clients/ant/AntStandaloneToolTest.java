package de.matrixweb.smaller.clients.ant;

import java.io.File;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.junit.Before;

import com.google.common.base.Joiner;

import de.matrixweb.smaller.AbstractToolTest;
import de.matrixweb.smaller.common.Manifest;
import de.matrixweb.smaller.common.Task;
import de.matrixweb.smaller.common.Version;

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
  protected void runToolChain(final Version version, final String file,
      final ToolChainCallback callback) throws Exception {
    if (Version.getCurrentVersion().isAtLeast(version)) {
      final SmallerTask stask = this.task;
      prepareTestFiles(file, callback, new ExecuteTestCallback() {
        @Override
        public void execute(final Manifest manifest, final File source,
            final File target) throws Exception {
          final Task task = manifest.getCurrent();
          stask.setProcessor(task.getProcessor());
          stask.setIn(Joiner.on(',').join(task.getIn()));
          stask.setOut(Joiner.on(',').join(task.getOut()));
          stask.setOptions(task.getOptionsDefinition());
          final File finalSource = source;
          stask.setFiles(new FileSet() {
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
          stask.setTarget(target);
          stask.execute();
        }
      });
    }
  }
}
