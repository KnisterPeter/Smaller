package de.matrixweb.smaller.clients.maven;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Test;

/**
 * @author markusw
 */
public class SmallerMojoTest extends AbstractMojoTestCase {

  /**
   * @throws Exception
   */
  @Test
  public void testMojoGoal() throws Exception {
    final File base = new File("target/smaller");
    FileUtils.deleteDirectory(base);

    final File testPom = new File(getBasedir(),
        "src/test/resources/smaller-maven-mojo-config.xml");
    final SmallerStandaloneMojo mojo = (SmallerStandaloneMojo) lookupMojo(
        "smaller", testPom);
    assertThat(mojo, is(notNullValue()));
    mojo.execute();
    assertThat(FileUtils.readFileToString(new File(base, "basic-min.js")),
        is("(function(){for(var e=0;10>e;e++)console.log(\"abcdef\")})()"));
    assertThat(FileUtils.readFileToString(new File(base, "style.css")),
        is("h1 h2 h3{text:#00f}"));
  }

}
