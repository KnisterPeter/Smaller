package de.matrixweb.smaller.osgi.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.matrixweb.smaller.common.Manifest;
import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.common.Task;
import de.matrixweb.smaller.common.Task.GlobalOptions;
import de.matrixweb.smaller.common.Zip;
import de.matrixweb.smaller.pipeline.Pipeline;
import de.matrixweb.smaller.pipeline.Result;
import de.matrixweb.smaller.resource.FileResourceResolver;
import de.matrixweb.smaller.resource.ResourceResolver;
import de.matrixweb.smaller.resource.Type;

/**
 * @author marwol
 */
public class Servlet extends HttpServlet {

  private static final Logger LOGGER = LoggerFactory.getLogger(Servlet.class);

  private static final long serialVersionUID = -3500628755781284892L;

  private final Pipeline pipeline;

  /**
   * @param pipeline
   */
  public Servlet(final Pipeline pipeline) {
    super();
    this.pipeline = pipeline;
  }

  /**
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void service(final HttpServletRequest request,
      final HttpServletResponse response) throws ServletException, IOException {
    LOGGER.info("Handle smaller request from {} {}", request.getRemoteAddr(),
        request.getRequestURI());
    final OutputStream out = response.getOutputStream();
    if ("/".equals(request.getRequestURI())) {
      executePipeline(request, response, out);
    } else {
      final PrintStream print = new PrintStream(out);
      print.print("hallo welt");
      out.close();
    }
  }

  private void executePipeline(final HttpServletRequest request,
      final HttpServletResponse response, final OutputStream out)
      throws IOException {
    Context context = null;
    try {
      context = setUpContext(request.getInputStream());
      final ResourceResolver resolver = new FileResourceResolver(
          context.sourceDir.getAbsolutePath());
      Task task = context.manifest.getNext();
      while (task != null) {
        writeResults(this.pipeline.execute(resolver, task), context.targetDir,
            task);
        task = context.manifest.getNext();
      }
      Zip.zip(out, context.targetDir);
      setResponseHeader(response, "OK", null);
    } catch (final SmallerException e) {
      LOGGER.error("Error during smaller execution", e);
      handleSmallerException(response, e);
    } catch (final IOException e) {
      LOGGER.error("Error during smaller execution", e);
      setResponseHeader(response, "ERROR", "Exception during execution");
    } finally {
      if (context != null) {
        context.inputZip.delete();
        FileUtils.deleteDirectory(context.sourceDir);
        FileUtils.deleteDirectory(context.targetDir);
      }
      IOUtils.closeQuietly(out);
    }
  }

  private Context setUpContext(final InputStream is) throws IOException {
    try {
      final Context context = unzip(is);
      final Manifest manifest = new ObjectMapper().readValue(
          getMainFile(context.sourceDir), Manifest.class);
      File output = context.sourceDir;
      if (GlobalOptions.isOutOnly(manifest.getTasks()[0])) {
        output = File.createTempFile("smaller-output", ".dir");
        output.delete();
        output.mkdirs();
      }
      context.targetDir = output;
      context.manifest = manifest;
      return context;
    } finally {
      IOUtils.closeQuietly(is);
    }
  }

  private Context unzip(final InputStream is) throws IOException {
    final Context context = storeZip(is);
    final File base = File.createTempFile("smaller-work", ".dir");
    base.delete();
    base.mkdir();
    Zip.unzip(context.inputZip, base);
    context.sourceDir = base;
    return context;
  }

  private Context storeZip(final InputStream in) throws IOException {
    final File temp = File.createTempFile("smaller-input", ".zip");
    temp.delete();
    FileOutputStream out = null;
    try {
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

    final Context context = new Context();
    context.inputZip = temp;
    return context;
  }

  private File getMainFile(final File input) {
    File main = new File(input, "META-INF/MAIN.json");
    if (!main.exists()) {
      // Old behaviour: Search directly in root of zip
      main = new File(input, "MAIN.json");
      if (!main.exists()) {
        throw new SmallerException(
            "Missing instructions file 'META-INF/MAIN.json'");
      }
    }
    return main;
  }

  private void writeResults(final Result result, final File outputDir,
      final Task task) throws IOException {
    writeResult(outputDir, task, result, Type.JS);
    writeResult(outputDir, task, result, Type.CSS);
  }

  private void writeResult(final File output, final Task task,
      final Result result, final Type type) throws IOException {
    final String outputFile = getTargetFile(output, task.getOut(), type);
    if (outputFile != null) {
      FileUtils.writeStringToFile(new File(outputFile), result.get(type)
          .getContents());
    }
  }

  private String getTargetFile(final File base, final String[] out,
      final Type type) {
    String target = null;
    for (final String s : out) {
      final String ext = FilenameUtils.getExtension(s);
      switch (type) {
      case JS:
        if (ext.equals("js")) {
          target = new File(base, s).getAbsolutePath();
        }
        break;
      case CSS:
        if (ext.equals("css")) {
          target = new File(base, s).getAbsolutePath();
        }
        break;
      default:
        throw new SmallerException("Invalid resource type " + type);
      }
    }
    return target;
  }

  private void handleSmallerException(final HttpServletResponse response,
      final SmallerException e) {
    final StringBuilder message = new StringBuilder(e.getMessage());
    Throwable t = e.getCause();
    while (t != null) {
      message.append(": ").append(t.getMessage());
      t = t.getCause();
    }
    setResponseHeader(response, "ERROR", message.toString());
  }

  private void setResponseHeader(final HttpServletResponse response,
      final String status, final String message) {
    response.setHeader("X-Smaller-Status", status);
    if (message != null) {
      response.setHeader("X-Smaller-Message", message);
    }
  }

  private static class Context {

    private File inputZip;

    private File sourceDir;

    private File targetDir;

    private Manifest manifest;

  }

}
