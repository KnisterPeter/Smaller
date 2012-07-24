package com.sinnerschrader.smaller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.sinnerschrader.smaller.common.Manifest;
import com.sinnerschrader.smaller.common.Manifest.Task.Options;

/**
 * @author marwol
 */
public class Router extends RouteBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(Router.class);

  /**
   * The property name of the current task working directory.
   */
  public static final String PROP_INPUT = "input";

  /**
   * The property name of the current task working directory.
   */
  public static final String PROP_OUTPUT = "output";

  private final ObjectMapper om = new ObjectMapper();

  private final ZipHandler zipHandler = new ZipHandler();

  private final TaskHandler taskHandler = new TaskHandler();

  private final ListenAddress listenaddress;

  private String version;

  /**
   * @param args
   */
  public Router(final String[] args) {
    listenaddress = new ListenAddress(args);
  }

  /**
   * @see org.apache.camel.builder.RouteBuilder#configure()
   */
  @Override
  public void configure() throws Exception {
    LOGGER.info("\nVersion: {}\nListen On: {}", this.getServer(), listenaddress);
    // @formatter:off
    
    this.from("jetty:http://" + listenaddress.httpAddress() + "?matchOnUriPrefix=true")
      .setExchangePattern(ExchangePattern.InOut)
      .bean(this, "storeZip")
      .doTry()
        .bean(zipHandler, "unzip")
        .bean(this, "parseMain")
        .dynamicRouter(this.bean(taskHandler, "runTask")).end()
        .bean(zipHandler, "zip")
      .doFinally()
        .bean(this, "cleanup")
      .end();
    
    this.from("direct:runAny").bean(taskHandler, "runAny");
    this.from("direct:runCoffeescript").bean(taskHandler, "runCoffeeScript");
    this.from("direct:runClosure").bean(taskHandler, "runClosure");
    this.from("direct:runUglifyjs").bean(taskHandler, "runUglifyJs");
    this.from("direct://runLessjs").bean(taskHandler, "runLessJs");
    this.from("direct://runSass").bean(taskHandler, "runSass");
    this.from("direct://runCssembed").bean(taskHandler, "runCssEmbed");
    this.from("direct://runYuicompressor").bean(taskHandler, "runYuiCompressor");
    // @formatter:on
  }

  /**
   * @param exchange
   * @throws IOException
   */
  public void storeZip(final Exchange exchange) throws IOException {
    final File temp = File.createTempFile("smaller-input", ".zip");
    temp.delete();
    InputStream in = null;
    FileOutputStream out = null;
    try {
      in = exchange.getIn().getBody(InputStream.class);
      if (in.available() <= 0) {
        throw new IOException("Invalid attachment size; rejecting request");
      } else {
        out = new FileOutputStream(temp);
        IOUtils.copy(in, out);
      }
    } finally {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
    }

    final RequestContext context = new RequestContext();
    context.setInputZip(temp);
    exchange.getOut().setBody(context);
  }

  /**
   * @param context
   * @return Returns the {@link RequestContext}
   * @throws IOException
   */
  public RequestContext parseMain(@Body final RequestContext context) throws IOException {
    final Manifest manifest = om.readValue(this.getMainFile(context.getInput()), Manifest.class);
    File output = context.getInput();
    final Set<Options> options = manifest.getTasks()[0].getOptions();
    if (options != null && options.contains(Options.OUT_ONLY)) {
      output = File.createTempFile("smaller-output", ".dir");
      output.delete();
      output.mkdirs();
    }
    context.setOutput(output);
    context.setManifest(manifest);
    return context;
  }

  private File getMainFile(final File input) {
    File main = new File(input, "META-INF/MAIN.json");
    if (!main.exists()) {
      // Old behaviour: Search directly in root of zip
      main = new File(input, "MAIN.json");
      if (!main.exists()) {
        throw new RuntimeException("Missing instructions file 'META-INF/MAIN.json'");
      }
    }
    return main;
  }

  /**
   * @param context
   * @return Returns the output stream
   * @throws IOException
   */
  public byte[] cleanup(@Body final RequestContext context) throws IOException {
    context.getInputZip().delete();
    FileUtils.deleteDirectory(context.getInput());
    FileUtils.deleteDirectory(context.getOutput());
    return context.getOutputZip().toByteArray();
  }

  private String getServer() {
    if (version != null) {
      return version;
    }
    synchronized (this) {
      if (version != null) {
        return version;
      }
      String v = "Smaller(development)";
      final InputStream is = Router.class.getClassLoader().getResourceAsStream("META-INF/maven/com.sinnerschrader.smaller/server/pom.xml");
      if (is != null) {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc;
        try {
          final DocumentBuilder db = dbf.newDocumentBuilder();
          doc = db.parse(is);
          v = "Smaller(" + doc.getElementsByTagName("version").item(0).getTextContent() + ")";
        } catch (final Exception e) {
          // System.out.println("IS:"+e.getMessage());
        }
      }
      version = v;
    }
    return version;
  }

  /**
   * @see org.apache.camel.builder.RouteBuilder#toString()
   */
  @Override
  public String toString() {
    return "Smaller Router";
  }

  /** */
  private static class ListenAddress {

    private String addr = "127.0.0.1";

    private String port = "1148";

    public ListenAddress(final String... params) {
      if (params.length == 1) {
        port = params[0];
      } else if (params.length >= 2) {
        port = params[0];
        addr = params[1];
      }
    }

    public String httpAddress() {
      return addr + ":" + port;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return this.httpAddress();
    }

  }

}
