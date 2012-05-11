/**
 * 
 */
package com.sinnerschrader.smaller.clients.maven;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * @author markusw
 */
public class SmallerMojoTest extends AbstractMojoTestCase {

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testMojoGoal() throws Exception {
    File testPom = new File(getBasedir(), "src/test/resources/smaller-maven-mojo-config.xml");
    SmallerMojo mojo = (SmallerMojo) lookupMojo("smaller", testPom);
    assertNotNull(mojo);
    mojo.execute();
  }

}
