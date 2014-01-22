package de.matrixweb.smaller.clients.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Project;
import org.junit.Before;

import de.matrixweb.smaller.AbstractToolTest;
import de.matrixweb.smaller.common.Manifest;
import de.matrixweb.smaller.common.ProcessDescription;
import de.matrixweb.smaller.common.ProcessDescription.Processor;
import de.matrixweb.smaller.common.Version;
import de.matrixweb.smaller.config.ConfigFile;
import de.matrixweb.smaller.config.Environment;
import static org.mockito.Mockito.*;

/**
 * @author marwol
 */
public class AntStandaloneToolTest extends AbstractToolTest {

  private Project project;

  private SmallerTask task;

  /** */
  @Before
  public void setupTask() {
    this.project = mock(Project.class);

    this.task = new SmallerTask();
    this.task.setProject(this.project);
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

          final ConfigFile configFile = new ConfigFile();
          configFile.setEnvironments(new HashMap<String, Environment>());
          for (final ProcessDescription processDescription : manifest
              .getProcessDescriptions()) {
            final Environment env = new Environment();
            env.getFiles().setFolder(new String[] { source.getAbsolutePath() });
            final List<String> names = new ArrayList<String>();
            for (final Processor proc : processDescription.getProcessors()) {
              names.add(proc.getName());
            }
            env.setPipeline(names.toArray(new String[0]));
            env.setProcess(new String[] { processDescription.getOutputFile() });
            for (final Processor proc : processDescription.getProcessors()) {
              final de.matrixweb.smaller.config.Processor processor = new de.matrixweb.smaller.config.Processor();
              processor.setSrc(processDescription.getInputFile());
              processor.setOptions(new HashMap<String, Object>(proc
                  .getOptions()));
              env.getProcessors().put(proc.getName(), processor);
            }
            configFile.getEnvironments().put(UUID.randomUUID().toString(), env);
          }
          final File testYml = new File("target/smaller-" + file + ".yml");
          FileUtils.write(testYml, configFile.dumpYaml());

          stask.setTarget(target);
          stask.setConfigFile(testYml);
          stask.execute();
        }
      });
    }
  }
}
