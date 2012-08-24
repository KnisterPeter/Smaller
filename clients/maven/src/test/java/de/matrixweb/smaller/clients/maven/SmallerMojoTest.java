/**
 * 
 */
package de.matrixweb.smaller.clients.maven;

import java.io.File;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.matrixweb.smaller.clients.maven.SmallerMojo;

import static org.junit.Assert.*;

import static org.hamcrest.CoreMatchers.*;

/**
 * @author markusw
 */
public class SmallerMojoTest extends AbstractMojoTestCase {

  private CamelContext camelContext;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Before
  public void setUp() throws Exception {
    super.setUp();
    camelContext = new DefaultCamelContext();
    camelContext.addRoutes(new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        from("jetty:http://localhost:1148/?matchOnUriPrefix=true").process(new Processor() {
          public void process(Exchange exchange) throws Exception {
            exchange.getOut().setBody(exchange.getIn().getBody());
          }
        });
      }
    });
    camelContext.start();
  }

  /**
   * @throws Exception
   */
  @After
  public void teardown() throws Exception {
    if (camelContext != null) {
      camelContext.stop();
      camelContext = null;
    }
    super.tearDown();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testMojoGoal() throws Exception {
    File base = new File("target/smaller");
    FileUtils.deleteDirectory(base);

    File testPom = new File(getBasedir(), "src/test/resources/smaller-maven-mojo-config.xml");
    SmallerMojo mojo = (SmallerMojo) lookupMojo("smaller", testPom);
    assertThat(mojo, is(notNullValue()));
    mojo.execute();
    assertThat(new File(base, "style.less").exists(), is(true));
    assertThat(new File(base, "a/code.js").exists(), is(true));
  }

}
