package de.matrixweb.smaller.nodejs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.ResourceIO;

/**
 * @author markusw
 */
public class NodejsExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodejsExecutor.class);

  private Process process;

  private File workingDir;

  private BufferedWriter output;

  private BufferedReader input;

  /**
   * 
   */
  public NodejsExecutor() {
    try {
      setupBinary();
    } finally {
      if (this.workingDir != null) {
        cleanupBinary();
      }
    }
  }

  private final void setupBinary() {
    try {
      this.workingDir = File.createTempFile("nodejs-v0.10.18", ".dir");
      this.workingDir.delete();
      this.workingDir.mkdirs();
      extractBinary(this.workingDir);

      final ProcessBuilder builder = new ProcessBuilder(new File(this.workingDir, getPlatformExecutable()).getAbsolutePath(), "ipc.js")
          .directory(this.workingDir);
      builder.environment().put("NODE_PATH", ".");
      this.process = builder.start();
    } catch (final IOException e) {
      throw new SmallerException("Unable to start node.js process", e);
    }
    try {
      this.output = new BufferedWriter(new OutputStreamWriter(this.process.getOutputStream(), "UTF-8"));
      this.input = new BufferedReader(new InputStreamReader(this.process.getInputStream(), "UTF-8"));
    } catch (final UnsupportedEncodingException e) {
      // Could not happend, since all JVMs must support UTF-8
    }
    try {
      if (!"ipc-ready".equals(this.input.readLine())) {
        throw new SmallerException("Unable to start node.js process:\n" + readStdError());
      }
    } catch (final IOException e) {
      throw new SmallerException("Unable to start node.js process", e);
    }
  }

  private final void cleanupBinary() {
    try {
      FileUtils.deleteDirectory(this.workingDir);
    } catch (final IOException e) {
      LOGGER.warn("Failed to delete node.js process directory", e);
    }
  }

  private String readStdError() throws IOException {
    final StringBuilder sb = new StringBuilder();
    final BufferedReader reader = new BufferedReader(new InputStreamReader(this.process.getErrorStream()));
    String line = reader.readLine();
    while (line != null) {
      sb.append(line).append('\n');
      line = reader.readLine();
    }
    return sb.toString();
  }

  private final void extractBinary(final File target) throws IOException {
    final File node = new File(target, getPlatformExecutable());
    copyFile("/v0.10.18/" + getPlatformPath() + "/" + getPlatformExecutable(), node);
    node.setExecutable(true, true);
    copyFile("/v0.10.18/ipc.js", new File(target, "ipc.js"));
  }

  private final String getPlatformPath() {
    final StringBuilder sb = new StringBuilder();
    if (SystemUtils.IS_OS_WINDOWS) {
      sb.append("win");
    } else if (SystemUtils.IS_OS_MAC_OSX) {
      sb.append("macos");
    } else if (SystemUtils.IS_OS_LINUX) {
      sb.append("linux");
    }
    if (SystemUtils.OS_ARCH.contains("64")) {
      sb.append("-x86_64");
    } else {
      sb.append("-x86");
    }
    return sb.toString();
  }

  private final String getPlatformExecutable() {
    if (SystemUtils.IS_OS_WINDOWS) {
      return "node.exe";
    }
    return "node";
  }

  private final void copyFile(final String inputFile, final File outputFile) throws IOException {
    InputStream in = null;
    try {
      in = getClass().getResourceAsStream(inputFile);
      if (in == null) {
        throw new FileNotFoundException(inputFile);
      }
      copyFile(in, outputFile);
    } finally {
      if (in != null) {
        IOUtils.closeQuietly(in);
      }
    }
  }

  private final void copyFile(final InputStream input, final File outputFile) throws IOException {
    FileOutputStream out = null;
    try {
      outputFile.getParentFile().mkdirs();
      out = new FileOutputStream(outputFile);
      IOUtils.copy(input, out);
    } finally {
      if (out != null) {
        IOUtils.closeQuietly(out);
      }
    }
  }

  /**
   * @param clazz
   * @param path
   * @throws IOException
   */
  public void addScriptFile(final Class<?> clazz, final String path) throws IOException {
    URL url = clazz.getResource(path);
    if (url == null) {
      throw new FileNotFoundException(path);
    }
    File file = new File(url.getPath());
    InputStream input = url.openStream();
    try {
      addScriptFile(input, file.getName());
    } finally {
      if (input != null) {
        IOUtils.closeQuietly(input);
      }
    }
  }

  /**
   * @param input
   *          The content of the script to add
   * @param name
   *          The script name
   * @throws IOException
   */
  public void addScriptFile(final InputStream input, final String name) throws IOException {
    copyFile(input, new File(this.workingDir, "module/" + name));
  }

  /**
   * @param command
   * @param resource
   * @param options
   * @return
   * @throws IOException
   */
  public Resource run(final Resource resource, final Map<String, String> options) throws IOException {
    synchronized (this.process) {
      assertNodeStillRunning();
      ResourceIO io = new ResourceIO();
      try {
        io.write(resource);

        ObjectMapper om = new ObjectMapper();

        Map<String, Object> command = new HashMap<String, Object>();
        command.put("path", io.getTarget());
        command.put("options", options);
        this.output.write(om.writeValueAsString(command));
        this.output.flush();
        System.out.println(this.input.readLine());

        return io.read();
      } finally {
        io.dispose();
      }
    }
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
   * 
   */
  public void dispose() {
    this.process.destroy();
    this.process = null;
    cleanupBinary();
  }

}
