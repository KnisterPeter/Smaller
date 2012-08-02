package com.sinnerschrader.smaller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.sinnerschrader.smaller.common.Manifest;
import com.sinnerschrader.smaller.common.Manifest.Task.Options;
import com.sinnerschrader.smaller.common.SmallerException;
import com.sinnerschrader.smaller.common.Zip;
import com.sinnerschrader.smaller.lib.ProcessorChain;

/**
 * @author marwol
 */
public class RequestHandler extends AbstractHandler {

  /**
   * @see org.eclipse.jetty.server.Handler#handle(java.lang.String,
   *      org.eclipse.jetty.server.Request,
   *      javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException,
      ServletException {
    final OutputStream out = baseRequest.getResponse().getOutputStream();
    Context context = null;
    try {
      context = this.setUpContext(baseRequest.getInputStream());
      new ProcessorChain().execute(context.sourceDir, context.targetDir, context.manifest);
      baseRequest.getResponse().setHeader("X-Smaller-Status", "OK");
      Zip.zip(out, context.targetDir);
    } catch (final SmallerException e) {
      final StringBuilder message = new StringBuilder(e.getMessage());
      Throwable t = e.getCause();
      while (t != null) {
        message.append(": ").append(t.getMessage());
        t = t.getCause();
      }
      baseRequest.getResponse().setHeader("X-Smaller-Status", "ERROR");
      baseRequest.getResponse().setHeader("X-Smaller-Message", message.toString());
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
      final Context context = this.unzip(is);
      final Manifest manifest = new ObjectMapper().readValue(this.getMainFile(context.sourceDir), Manifest.class);
      File output = context.sourceDir;
      final Set<Options> options = manifest.getTasks()[0].getOptions();
      if (options != null && options.contains(Options.OUT_ONLY)) {
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
    final Context context = this.storeZip(is);
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
        throw new SmallerException("Missing instructions file 'META-INF/MAIN.json'");
      }
    }
    return main;
  }

  private static class Context {

    File inputZip;

    File sourceDir;

    File targetDir;

    Manifest manifest;

  }

}
