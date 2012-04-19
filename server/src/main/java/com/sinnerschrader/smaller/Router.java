package com.sinnerschrader.smaller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Property;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * @author marwol
 */
public class Router extends RouteBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(Router.class);

  /**
   * The property name of the current task working directory.
   */
  public static final String PROP_DIRECTORY = "directory";

  private ObjectMapper om = new ObjectMapper();

  private ZipHandler zipHandler = new ZipHandler();

  private TaskHandler taskHandler = new TaskHandler();

  private ListenAddress listenaddress;

  private String version;

  /**
   * @param args
   */
  public Router(String[] args) {
    listenaddress = new ListenAddress(args);
  }

  /**
   * @see org.apache.camel.builder.RouteBuilder#configure()
   */
  @Override
  public void configure() throws Exception {
    LOGGER.info("\nVersion: {}\nListen On: {}", getServer(), listenaddress);
    // @formatter:off
    
    from("jetty:http://" + listenaddress.httpAddress() + "?matchOnUriPrefix=true")
      .setExchangePattern(ExchangePattern.InOut)
      .doTry()
        .bean(this, "storeZip")
        .to("seda:request-queue")
      .doFinally()
        .bean(this, "cleanup")
      .end();
    
    from("seda:request-queue?concurrentConsumers=1&blockWhenFull=true")
      .bean(zipHandler, "unzip")
      .bean(this, "parseMain")
      .dynamicRouter(bean(taskHandler, "runTask"))
      .bean(zipHandler, "zip");
    
    from("direct:runClosure").bean(taskHandler, "runClosure");
    from("direct:runUglifyjs").bean(taskHandler, "runUglifyJs");
    // @formatter:on
  }

  /**
   * @param exchange
   * @throws IOException
   */
  public void storeZip(Exchange exchange) throws IOException {
    File temp = File.createTempFile("smaller-", ".zip");
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
    exchange.getOut().setBody(temp);
  }

  /**
   * @param base
   * @return the parsed manifest
   * @throws IOException
   */
  public Manifest parseMain(@Property(PROP_DIRECTORY) File base) throws IOException {
    return om.readValue(new File(base, "MAIN.json"), Manifest.class);
  }

  /**
   * @param base
   * @throws IOException
   */
  public void cleanup(@Property(PROP_DIRECTORY) File base) throws IOException {
    FileUtils.deleteDirectory(base);
  }

  private String getServer() {
    if (version != null) {
      return version;
    }
    synchronized (this) {
      if (version != null) {
        return version;
      }
      String version = "Smaller(development)";
      final InputStream is = Router.class.getClassLoader().getResourceAsStream("META-INF/maven/com.sinnerschrader/smaller/pom.xml");
      if (is != null) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc;
        try {
          DocumentBuilder db = dbf.newDocumentBuilder();
          doc = db.parse(is);
          version = "Smaller(" + doc.getElementsByTagName("version").item(0).getTextContent() + ")";
        } catch (Exception e) {
          // System.out.println("IS:"+e.getMessage());
        }
      }
      this.version = version;
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

    public ListenAddress(String... params) {
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
      return httpAddress();
    }

  }

}
