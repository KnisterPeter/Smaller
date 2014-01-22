package de.matrixweb.smaller.clients.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.PlexusTestCase;
import org.junit.After;
import org.junit.Before;

import de.matrixweb.smaller.AbstractToolTest;
import de.matrixweb.smaller.common.Manifest;
import de.matrixweb.smaller.common.ProcessDescription;
import de.matrixweb.smaller.common.ProcessDescription.Processor;
import de.matrixweb.smaller.common.Version;
import de.matrixweb.smaller.config.ConfigFile;
import de.matrixweb.smaller.config.Environment;

/**
 * @author markusw
 */
public class SmallerMojoTest extends AbstractToolTest {

  private final MojoTest mojoTest = new MojoTest();

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    this.mojoTest.setUp();
  }

  /**
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {
    this.mojoTest.tearDown();
  }

  /**
   * @see de.matrixweb.smaller.AbstractBaseTest#runToolChain(java.lang.String,
   *      de.matrixweb.smaller.AbstractBaseTest.ToolChainCallback)
   */
  @Override
  protected void runToolChain(final Version minimum, final String file,
      final ToolChainCallback callback) throws Exception {
    if (Version.getCurrentVersion().isAtLeast(minimum)) {
      prepareTestFiles(file, callback, new ExecuteTestCallback() {
        @Override
        public void execute(final Manifest manifest, final File source,
            final File target) throws Exception {

          FileUtils.copyFile(new File(PlexusTestCase.getBasedir(),
              "src/test/resources/smaller.yml"),
              new File(PlexusTestCase.getBasedir(), "target/smaller-" + file
                  + ".yml"));
          final File testYml = new File(PlexusTestCase.getBasedir(),
              "target/smaller-" + file + ".yml");

          final ConfigFile configFile = ConfigFile.read(testYml);
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
          FileUtils.write(testYml, configFile.dumpYaml());

          FileUtils.copyFile(new File(PlexusTestCase.getBasedir(),
              "src/test/resources/smaller-maven-mojo-config.xml"), new File(
              PlexusTestCase.getBasedir(), "target/smaller-maven-mojo-config-"
                  + file + ".xml"));
          final File testPom = new File(PlexusTestCase.getBasedir(),
              "target/smaller-maven-mojo-config-" + file + ".xml");
          String pomData = FileUtils.readFileToString(testPom, "UTF-8");
          pomData = pomData.replace("<target>target/smaller</target>",
              "<target>" + target.getPath() + "</target>");
          pomData = pomData
              .replace(
                  "<config-file>${basedir}/src/test/resources/smaller.yml</config-file>",
                  "<config-file>" + testYml.getAbsolutePath()
                      + "</config-file>");
          FileUtils.write(testPom, pomData);

          final SmallerStandaloneMojo mojo = (SmallerStandaloneMojo) SmallerMojoTest.this.mojoTest
              .lookupMojo("smaller", testPom);
          mojo.execute();
        }
      });
    }
  }

  private static class MojoTest extends AbstractMojoTestCase {

    @Override
    protected void setUp() throws Exception {
      super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
      super.tearDown();
    }

    @Override
    protected Mojo lookupMojo(final String goal, final File pom)
        throws Exception {
      return super.lookupMojo(goal, pom);
    }

  }

}
