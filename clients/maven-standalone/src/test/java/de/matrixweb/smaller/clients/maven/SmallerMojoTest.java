package de.matrixweb.smaller.clients.maven;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.PlexusTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.matrixweb.smaller.AbstractToolTest;
import de.matrixweb.smaller.common.Manifest;
import de.matrixweb.smaller.common.Version;

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
          final de.matrixweb.smaller.common.Task task = manifest.getCurrent();

          final File testPom = new File(PlexusTestCase.getBasedir(),
              "target/smaller-maven-mojo-config-" + file + ".xml");
          FileUtils.copyFile(new File(PlexusTestCase.getBasedir(),
              "src/test/resources/smaller-maven-mojo-config.xml"), new File(
              PlexusTestCase.getBasedir(), "target/smaller-maven-mojo-config-"
                  + file + ".xml"));
          String pomData = FileUtils.readFileToString(testPom, "UTF-8");
          pomData = pomData.replace("<target>target/smaller</target>",
              "<target>" + target.getPath() + "</target>");
          pomData = pomData.replace(
              "<directory>src/test/resources/dir</directory>", "<directory>"
                  + source.getAbsolutePath() + "</directory>");
          pomData = pomData
              .replace(
                  "<processor>closure,uglifyjs,lessjs,yuiCompressor,cssembed</processor>",
                  "<processor>" + task.getProcessor() + "</processor>");
          pomData = pomData.replace("<in>basic.json,style.less</in>", "<in>"
              + StringUtils.join(task.getIn(), ',') + "</in>");
          pomData = pomData.replace("<out>basic-min.js,style.css</out>",
              "<out>" + StringUtils.join(task.getOut(), ',') + "</out>");
          FileUtils.write(testPom, pomData);

          final SmallerStandaloneMojo mojo = (SmallerStandaloneMojo) SmallerMojoTest.this.mojoTest
              .lookupMojo("smaller", testPom);
          mojo.execute();
        }
      });
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testMojoGoal() throws Exception {
    final File base = new File("target/smaller");
    FileUtils.deleteDirectory(base);

    final File testPom = new File(PlexusTestCase.getBasedir(),
        "src/test/resources/smaller-maven-mojo-config.xml");
    final SmallerStandaloneMojo mojo = (SmallerStandaloneMojo) this.mojoTest
        .lookupMojo("smaller", testPom);
    assertThat(mojo, is(notNullValue()));
    mojo.execute();
    assertThat(FileUtils.readFileToString(new File(base, "basic-min.js")),
        is("(function(){for(var e=0;10>e;e++)console.log(\"abcdef\")})()"));
    assertThat(FileUtils.readFileToString(new File(base, "style.css")),
        is("h1 h2 h3{text:#00f}"));
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
