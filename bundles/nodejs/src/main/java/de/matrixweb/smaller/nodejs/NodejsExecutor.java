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
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.ResourceGroup;
import de.matrixweb.vfs.VFS;

/**
 * @author markusw
 */
public class NodejsExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodejsExecutor.class);

  private final String version = "0.10.18";

  private final ObjectMapper om = new ObjectMapper();

  private Process process;

  private File workingDir;

  private BufferedWriter output;

  private BufferedReader input;

  /**
   * Creates a new node.js bridge and setup the working directory.
   */
  public NodejsExecutor() {
    try {
      setupBinary();
    } catch (SmallerException e) {
      if (this.workingDir != null) {
        cleanupBinary();
      }
      throw e;
    }
  }

  private final void setupBinary() {
    try {
      this.workingDir = File.createTempFile("nodejs-v" + this.version, ".dir");
      this.workingDir.delete();
      this.workingDir.mkdirs();
      extractBinary(this.workingDir);
    } catch (final IOException e) {
      throw new SmallerException("Unable to setup the node folder", e);
    }
  }

  private final void cleanupBinary() {
    try {
      FileUtils.deleteDirectory(this.workingDir);
    } catch (final IOException e) {
      LOGGER.warn("Failed to delete node.js process directory", e);
    }
  }

  private final void extractBinary(final File target) throws IOException {
    final File node = new File(target, getPlatformExecutable());
    copyFile("/v" + this.version + "/" + getPlatformPath() + "/" + getPlatformExecutable(), node);
    node.setExecutable(true, true);
    copyFile("/v" + this.version + "/ipc.js", new File(target, "ipc.js"));
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
      FileUtils.copyInputStreamToFile(in, outputFile);
    } finally {
      if (in != null) {
        IOUtils.closeQuietly(in);
      }
    }
  }

  /**
   * Adds a npm-module folder to the bridge to be called from Java.
   * 
   * @param cl
   *          The {@link ClassLoader} to search the module from (required to be
   *          OSGi capable)
   * @param path
   *          The path or name of the npm-module to install relative to the
   *          class-path root
   * @throws IOException
   *           Thrown if the installation of the npm-module fails
   */
  public void addModule(final ClassLoader cl, final String path) throws IOException {
    final Enumeration<URL> urls = cl.getResources(path);
    while (urls.hasMoreElements()) {
      copyModuleToWorkingDirectory(urls.nextElement());
    }
  }

  private void copyModuleToWorkingDirectory(final URL url) throws IOException {
    try {
      if ("file".equals(url.getProtocol())) {
        copyModuleFromFolder(url);
      } else if ("jar".equals(url.getProtocol())) {
        copyModuleFromJar(url);
      } else {
        throw new SmallerException("Unsupported url schema: " + url);
      }
    } catch (final URISyntaxException e) {
      throw new IOException("Invalid uri syntax", e);
    }
  }

  private void copyModuleFromJar(final URL url) throws IOException {
    String str = url.toString();
    String path = str.substring(str.indexOf('!') + 2);
    str = str.substring("jar:file:".length(), str.indexOf('!'));
    JarFile jar = new JarFile(str);
    try {
      Enumeration<JarEntry> e = jar.entries();
      while (e.hasMoreElements()) {
        JarEntry entry = e.nextElement();
        if (entry.getName().startsWith(path) && !entry.isDirectory()) {
          File target = new File(this.workingDir, entry.getName().substring(path.length()));
          target.getParentFile().mkdirs();
          InputStream in = jar.getInputStream(entry);
          FileUtils.copyInputStreamToFile(in, target);
        }
      }
    } finally {
      jar.close();
    }
  }

  private void copyModuleFromFolder(final URL url) throws URISyntaxException, IOException {
    final File file = new File(url.toURI());
    if (file.isDirectory()) {
      FileUtils.copyDirectory(file, this.workingDir);
    } else {
      FileUtils.copyFileToDirectory(file, this.workingDir);
    }
  }

  /**
   * Executes the installed npm-module.
   * 
   * The given VFS is given node as input files to operate on.
   * 
   * @param vfs
   *          The {@link VFS} to operate on.
   * @param resource
   * @param options
   *          A map of options given to the node process
   * @return Returns the node.js processed result
   * @throws IOException
   */
  public Resource run(final VFS vfs, final Resource resource, final Map<String, String> options) throws IOException {
    startNodeIfRequired();
    synchronized (this.process) {
      Resource input = null;
      if (resource instanceof ResourceGroup) {
        input = ((ResourceGroup) resource).getResources().get(0);
      } else {
        input = resource;
      }

      File temp = File.createTempFile("smaller-node-resource", ".dir");
      try {
        temp.delete();
        temp.mkdirs();
        File infolder = new File(temp, "input");
        infolder.mkdirs();
        File outfolder = new File(temp, "output");
        outfolder.mkdirs();

        vfs.exportFS(infolder);

        try {
          String resultPath = callNode(input, infolder, outfolder, options);
          vfs.stack();
          vfs.importFS(outfolder);
          return resultPath == null ? input : input.getResolver().resolve(resultPath);
        } catch (NodeJsException e) {
          return resource;
        }
      } finally {
        FileUtils.deleteDirectory(temp);
      }
    }
  }

  private String callNode(final Resource resource, final File infolder, final File outfolder,
      final Map<String, String> options) throws IOException, JsonGenerationException, JsonMappingException,
      JsonParseException, NodeJsException {
    String resultPath = null;

    final Map<String, Object> command = new HashMap<String, Object>();
    command.put("cwd", this.workingDir.getAbsolutePath());
    command.put("indir", infolder.getAbsolutePath());
    if (resource != null) {
      command.put("file", resource.getPath());
    }
    command.put("outdir", outfolder.getAbsolutePath());
    command.put("options", options);
    this.output.write(this.om.writeValueAsString(command) + '\n');
    this.output.flush();
    waitForResponse();
    String error = readStdError();
    if (error != null) {
      // TODO: Reconsider error handling
      LOGGER.error(error);
      throw new NodeJsException();
    } else {
      // TODO: Implement result handling
      final Map<String, Object> map = this.om.readValue(this.input.readLine(),
          new TypeReference<Map<String, Object>>() {
          });
      if (map.containsKey("stdout")) {
        for (String line : (List<String>) map.get("stdout")) {
          LOGGER.info(line);
        }
      }
      if (map.containsKey("stderr")) {
        for (String line : (List<String>) map.get("stderr")) {
          LOGGER.error(line);
        }
      }
      if (map.containsKey("error")) {
        // TODO: Reconsider error handling
        LOGGER.error(map.get("error").toString());
        throw new NodeJsException();
      }
      if (map.containsKey("result")) {
        resultPath = map.get("result").toString();
      }
    }
    return resultPath;
  }

  private void startNodeIfRequired() throws IOException {
    try {
      if (this.process != null) {
        this.process.exitValue();
      }
      try {
        final ProcessBuilder builder = new ProcessBuilder(
            new File(this.workingDir, getPlatformExecutable()).getAbsolutePath(), "ipc.js").directory(this.workingDir);
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
    } catch (final IllegalThreadStateException e) {
      // Just ignore and continue
    }
  }

  private void waitForResponse() throws IOException {
    while (this.process.getInputStream().available() == 0 && this.process.getErrorStream().available() == 0) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // This could savely be ignored
      }
    }
  }

  private String readStdError() throws IOException {
    if (this.process.getErrorStream().available() > 0) {
      final StringBuilder sb = new StringBuilder();
      final BufferedReader reader = new BufferedReader(new InputStreamReader(this.process.getErrorStream()));
      String line = reader.readLine();
      while (line != null) {
        sb.append(line).append('\n');
        line = reader.readLine();
      }
      return sb.toString();
    }
    return null;
  }

  /**
   * Must be called when stopping the node-bridge to cleanup temporary
   * resources.
   */
  public void dispose() {
    if (this.process != null) {
      this.process.destroy();
      this.process = null;
    }
    cleanupBinary();
  }

  private static class NodeJsException extends Exception {

    private static final long serialVersionUID = -1803769150577336117L;

  }

}
