package com.sinnerschrader.smaller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.activation.DataHandler;
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
      .to("direct:handle-request");
    
    from("netty:tcp://" + listenaddress.tcpAddress() + "?sync=true")
      .setExchangePattern(ExchangePattern.InOut)
      .to("direct:handle-request");
    
    from("direct:handle-request")
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
      // in = exchange.getIn().getBody(InputStream.class);
      Map<String, DataHandler> attachments = exchange.getIn().getAttachments();
      if (/* in.available() <= 0 */attachments.size() != 1) {
        throw new IOException("Invalid attachment size; rejecting request");
      } else {
        in = attachments.entrySet().iterator().next().getValue().getDataSource().getInputStream();
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
  public Manifest parseMain(@Property("directory") File base) throws IOException {
    return om.readValue(new File(base, "MAIN.json"), Manifest.class);
  }

  /**
   * @param base
   * @throws IOException
   */
  public void cleanup(@Property("directory") File base) throws IOException {
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

    private String tcpPort = "1149";

    public ListenAddress(String... params) {
      if (params.length == 1) {
        port = params[0];
      } else if (params.length >= 2) {
        port = params[0];
        addr = params[1];
      } else if (params.length >= 3) {
        port = params[0];
        tcpPort = params[1];
        addr = params[2];
      }
    }

    public String httpAddress() {
      return addr + ":" + port;
    }

    public String tcpAddress() {
      return addr + ":" + tcpPort;
    }

  }

}
