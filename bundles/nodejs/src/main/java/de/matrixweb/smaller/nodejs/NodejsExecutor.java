package de.matrixweb.smaller.nodejs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.ResourceIO;

/**
 * @author markusw
 */
public class NodejsExecutor {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodejsExecutor.class);

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

      final ProcessBuilder builder = new ProcessBuilder(new File(
          this.workingDir, getPlatformExecutable()).getAbsolutePath(), "ipc.js")
          .directory(this.workingDir);
      builder.environment().put("NODE_PATH", ".");
      this.process = builder.start();
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
    try {
      if (!"ipc-ready".equals(this.input.readLine())) {
        throw new SmallerException("Unable to start node.js process:\n"
            + readStdError());
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
    final BufferedReader reader = new BufferedReader(new InputStreamReader(
        this.process.getErrorStream()));
    String line = reader.readLine();
    while (line != null) {
      sb.append(line).append('\n');
      line = reader.readLine();
    }
    return sb.toString();
  }

  private final void extractBinary(final File target) throws IOException {
    final File node = new File(target, getPlatformExecutable());
    copyFile("/v0.10.18/" + getPlatformPath() + "/" + getPlatformExecutable(),
        node);
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

  private final void copyFile(final String inputFile, final File outputFile)
      throws IOException {
    InputStream in = null;
    try {
      in = getClass().getResourceAsStream(inputFile);
      if (in == null) {
        throw new FileNotFoundException(inputFile);
      }
      FileUtils.copyInputStreamToFile(in, outputFile);
    } finally {
      if (in != null) {
        IOUtils.closeQuietly(in);
      }
    }
  }

  /**
   * @param clazz
   * @param path
   * @throws IOException
   */
  public void addModule(final Class<?> clazz, final String path)
      throws IOException {
    final Enumeration<URL> urls = clazz.getClassLoader().getResources(path);
    while (urls.hasMoreElements()) {
      copyModuleToWorkingDirectory(urls.nextElement());
    }
  }

  private void copyModuleToWorkingDirectory(final URL url) throws IOException {
    try {
      if ("file".equals(url.getProtocol())) {
        final File file = new File(url.toURI());
        if (file.isDirectory()) {
          FileUtils.copyDirectory(file, this.workingDir);
        } else {
          FileUtils.copyFileToDirectory(file, this.workingDir);
        }
      } else {
        throw new SmallerException("Unsupported url schema: " + url);
      }
    } catch (final URISyntaxException e) {
      throw new IOException("Invalid uri syntax", e);
    }
  }

  /**
   * @param command
   * @param resource
   * @param options
   * @return
   * @throws IOException
   */
  public Resource run(final Resource resource, final Map<String, String> options)
      throws IOException {
    synchronized (this.process) {
      assertNodeStillRunning();
      final ResourceIO io = new ResourceIO();
      try {
        io.write(resource);

        final ObjectMapper om = new ObjectMapper();

        final Map<String, Object> command = new HashMap<String, Object>();
        command.put("cwd", this.workingDir.getAbsolutePath());
        command.put("path", io.getTarget());
        command.put("options", options);
        this.output.write(om.writeValueAsString(command));
        this.output.flush();
        final Map<String, Object> map = om.readValue(this.input.readLine(),
            new TypeReference<Map<String, Object>>() {
            });
        System.out.println(map);
        map.get("stdout");
        map.get("stderr");
        if (map.containsKey("error")) {
          LOGGER.error(map.get("error").toString());
        }
        final Resource output = io.read();
        System.out.println(output.getContents());
        return output;
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