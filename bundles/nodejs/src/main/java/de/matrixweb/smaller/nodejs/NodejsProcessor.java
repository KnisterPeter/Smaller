package de.matrixweb.smaller.nodejs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.Type;

/**
 * @author markusw
 */
public class NodejsProcessor implements Processor {

  private Process process;

  private File workingDir;

  private BufferedWriter output;

  private BufferedReader input;

  /**
   * 
   */
  public NodejsProcessor() {
    try {
      this.workingDir = File.createTempFile("nodejs-v0.10.18", ".dir");
      this.workingDir.delete();
      this.workingDir.mkdirs();
      extractBinary(this.workingDir);

      this.process = new ProcessBuilder(Arrays.asList(new File(this.workingDir,
          "node").getAbsolutePath())).directory(this.workingDir)
          .redirectError(Redirect.INHERIT).start();
    } catch (final IOException e) {
      throw new SmallerException("Unable to start node.js process", e);
    }
    try {
      this.output = new BufferedWriter(new OutputStreamWriter(
          this.process.getOutputStream(), "UTF-8"));
      this.input = new BufferedReader(new InputStreamReader(
          this.process.getInputStream(), "UTF-8"));
    } catch (final UnsupportedEncodingException e) {
      // Could not happend, since all JVMs must support UTF-8
    }
  }

  private final void extractBinary(final File target) throws IOException {
    InputStream in = null;
    FileOutputStream out = null;
    try {
      final File node = new File(target, "node");
      // TODO: Locate correct one for version and platform
      in = getClass().getResourceAsStream("/v0.10.18/linux-x86_64/node");
      out = new FileOutputStream(node);
      IOUtils.copy(in, out);
      node.setExecutable(true, true);
    } finally {
      if (in != null) {
        IOUtils.closeQuietly(in);
      }
      if (out != null) {
        IOUtils.closeQuietly(out);
      }
    }
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#supportsType(de.matrixweb.smaller.resource.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return true;
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.smaller.resource.Resource,
   *      java.util.Map)
   */
  @Override
  public synchronized Resource execute(final Resource resource,
      final Map<String, String> options) throws IOException {
    System.out.println("NodejsProcessor.execute() 0");
    assertNodeStillRunning();
    System.out.println("NodejsProcessor.execute() 1");
    this.output.write("console.log('yada\n')");
    System.out.println("NodejsProcessor.execute() 2");
    for (int i = 0; i < 10; i++) {
      System.out.print((char) this.input.read());
    }
    System.out.println(this.input.readLine());
    System.out.println("NodejsProcessor.execute() 3");
    return resource;
  }

  private void assertNodeStillRunning() throws IOException {
    try {
      final int code = this.process.exitValue();
      throw new IOException("node.js process already died with code " + code);
    } catch (final IllegalThreadStateException e) {
      // Just ignore and continue
    }
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#dispose()
   */
  @Override
  public void dispose() {
    this.process.destroy();
    this.process = null;
  }

}
