package com.sinnerschrader.smaller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.activation.DataHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.sinnerschrader.minificator.Closure;
import com.sinnerschrader.minificator.ExecutionException;
import com.sinnerschrader.smaller.Main.Task;

/**
 * @author marwol
 */
public class Router extends RouteBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(Router.class);

  private ObjectMapper om = new ObjectMapper();

  private ListenAddress listenaddress;

  private String version;

  /**
   * @param args
   */
  public Router(String[] args) {
    if (args.length == 1) {
      listenaddress = new ListenAddress(args[0], null);
    } else if (args.length >= 2) {
      listenaddress = new ListenAddress(args[0], args[1]);
    } else {
      listenaddress = new ListenAddress();
    }
  }

  /**
   * @see org.apache.camel.builder.RouteBuilder#configure()
   */
  @Override
  public void configure() throws Exception {
    LOGGER.info("\nVersion: {}\nListen On: {}", getServer(), listenaddress);
    // @formatter:off
    
    from("jetty:http://" + listenaddress + "?matchOnUriPrefix=true")
      .doTry()
        .bean(this, "storeZip")
        .to("seda:request-queue")
      .doFinally()
        .bean(this, "cleanup")
      .end();
    
    from("seda:request-queue?concurrentConsumers=1&blockWhenFull=true")
      .bean(this, "unzip")
      .bean(this, "parseMain")
      .dynamicRouter(bean(this, "runTask"))
      .bean(this, "zip");
    
    from("direct:runClosure").bean(this, "runClosure");
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
   * @param exchange
   * @throws IOException
   */
  public void unzip(Exchange exchange) throws IOException {
    File base = File.createTempFile("smaller-", ".dir");
    base.delete();
    base.mkdir();

    File temp = exchange.getIn().getBody(File.class);
    ZipFile zipFile = new ZipFile(temp);
    try {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (entry.isDirectory()) {
          FileUtils.forceMkdir(new File(base, entry.getName()));
        } else {
          InputStream in = null;
          FileOutputStream out = null;
          try {
            in = zipFile.getInputStream(entry);
            out = new FileOutputStream(new File(base, entry.getName()));
            IOUtils.copy(in, out);
          } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
          }
        }
      }
    } finally {
      zipFile.close();
    }
    temp.delete();
    exchange.setProperty("directory", base);
  }

  /**
   * @param exchange
   * @throws IOException
   */
  public void parseMain(Exchange exchange) throws IOException {
    exchange.getOut().setBody(om.readValue(new File(exchange.getProperty("directory", File.class), "MAIN.json"), Main.class));
  }

  /**
   * @param exchange
   * @return the route name of the next step
   */
  public String runTask(Exchange exchange) {
    Main main = exchange.getIn().getBody(Main.class);
    Task task = main.getNext();
    if (task == null) {
      return null;
    }
    return "direct:run" + StringUtils.capitalize(task.getProcessor().toLowerCase());
  }

  /**
   * @param exchange
   * @throws Exception
   */
  public void runClosure(Exchange exchange) throws Exception {
    File base = exchange.getProperty("directory", File.class);
    Task task = exchange.getIn().getBody(Main.class).getCurrent();

    Closure closure = new Closure(new com.sinnerschrader.minificator.Logger() {
      public void info(String message) {
        LOGGER.info(message);
      }
    });
    closure.setBaseDir(base);
    closure.setJson(true);
    closure.setClosureSourceFiles(task.getIn());
    closure.setClosureTargetFile(new File(base, task.getOut()[0]));
    try {
      closure.run();
    } catch (ExecutionException e) {
      throw new Exception("Failed to run closure", e);
    }
  }

  /**
   * @param exchange
   * @throws IOException 
   */
  public void zip(Exchange exchange) throws IOException {
    File base = exchange.getProperty("directory", File.class);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(baos);
    zipDirectory(zos, base);
    zos.close();
    exchange.getOut().setHeader("Content-Type", "application/zip");
    exchange.getOut().setBody(baos);
  }

  private void zipDirectory(ZipOutputStream zos, File base) throws IOException {
    String[] dirList = base.list();
    for (int i = 0; i < dirList.length; i++) {
      File f = new File(base, dirList[i]);
      if (f.isDirectory()) {
        zipDirectory(zos, f);
      } else {
        FileInputStream fis = new FileInputStream(f);
        ZipEntry anEntry = new ZipEntry(f.getPath());
        zos.putNextEntry(anEntry);
        IOUtils.copy(fis, zos);
        fis.close();
      }
    }
  }

  /**
   * @param exchange
   * @throws IOException
   */
  public void cleanup(Exchange exchange) throws IOException {
    File base = exchange.getProperty("directory", File.class);
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

    public ListenAddress() {
    }

    public ListenAddress(String port, String addr) {
      this.port = port;
      if (addr != null) {
        this.addr = addr;
      }
    }

    public String toString() {
      return addr + ":" + port;
    }
  }

}
