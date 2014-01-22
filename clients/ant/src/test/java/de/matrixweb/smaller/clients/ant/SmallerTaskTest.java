package de.matrixweb.smaller.clients.ant;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildFileTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author marwol
 */
public class SmallerTaskTest extends BuildFileTest {

  private CamelContext camelContext;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.camelContext = new DefaultCamelContext();
    this.camelContext.addRoutes(new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        from("jetty:http://localhost:1148/?matchOnUriPrefix=true").process(
            new Processor() {
              public void process(final Exchange exchange) throws Exception {
                exchange.getOut().setBody(exchange.getIn().getBody());
              }
            });
      }
    });
    this.camelContext.start();
  }

  /**
   * @throws Exception
   */
  @After
  public void teardown() throws Exception {
    if (this.camelContext != null) {
      this.camelContext.stop();
      this.camelContext = null;
    }
    super.tearDown();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSmaller() throws Exception {
    final File base = new File("target/smaller");
    FileUtils.deleteDirectory(base);

    configureProject("src/test/resources/default/build.xml");
    executeTarget("smaller");

    assertThat("style.less should exist in " + base, new File(base,
        "style.less").exists(), is(true));
    assertThat("a/code.js should exist in " + base,
        new File(base, "a/code.js").exists(), is(true));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testOutOnly() throws Exception {
    final File base = new File("target/smaller");
    FileUtils.deleteDirectory(base);

    configureProject("src/test/resources/out-only/build.xml");
    executeTarget("smaller");

    assertThat("style.less should exist in " + base, new File(base,
        "style.less").exists(), is(true));
    assertThat("a/code.js should exist in " + base,
        new File(base, "a/code.js").exists(), is(true));
  }

}
