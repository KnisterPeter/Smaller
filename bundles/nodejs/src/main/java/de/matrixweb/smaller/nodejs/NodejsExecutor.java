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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.ResourceResolver;
import de.matrixweb.smaller.resource.ResourceUtil;
import de.matrixweb.smaller.resource.StringResource;

/**
 * @author markusw
 */
public class NodejsExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodejsExecutor.class);

  private final String version = "0.10.18";

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
   * @param clazz
   * @param path
   * @throws IOException
   */
  public void addModule(final Class<?> clazz, final String path) throws IOException {
    final Enumeration<URL> urls = clazz.getClassLoader().getResources(path);
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
          File target = new File(this.workingDir, entry.getName());
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
   * @param command
   * @param resource
   * @param options
   * @return
   * @throws IOException
   */
  public Resource run(final Resource resource, final Map<String, String> options) throws IOException {
    startNodeIfRequired();
    synchronized (this.process) {
      try {
        File infolder = resource.getResolver().writeAll();
        File temp = File.createTempFile("smaller-resource", ".dir");
        temp.delete();
        temp.mkdirs();
        FileUtils.moveDirectoryToDirectory(infolder, new File(temp, "input"), true);
        infolder = new File(temp, "input");
        File outfolder = new File(temp, "output");
        outfolder.mkdirs();

        final ObjectMapper om = new ObjectMapper();

        final Map<String, Object> command = new HashMap<String, Object>();
        command.put("cwd", this.workingDir.getAbsolutePath());
        command.put("path", infolder.getAbsolutePath());
        command.put("in", resource.getRelativePath());
        command.put("out", outfolder.getAbsolutePath());
        command.put("options", options);
        this.output.write(om.writeValueAsString(command) + '\n');
        this.output.flush();
        waitForResponse();
        String error = readStdError();
        if (error != null) {
          LOGGER.error(error);
        } else {
          final Map<String, Object> map = om.readValue(this.input.readLine(), new TypeReference<Map<String, Object>>() {
          });
          // System.out.println(map);
          // map.get("stdout");
          // map.get("stderr");
          if (map.containsKey("result")) {
            LOGGER.info(map.get("result").toString());
          }
          if (map.containsKey("error")) {
            LOGGER.error(map.get("error").toString());
          }
        }

        Resource result = ResourceUtil
            .createResourceGroup(new TempResourceResolver(outfolder.getAbsolutePath()),
                getOutputFiles(outfolder, outfolder)).getByType(resource.getType()).get(0);
        FileUtils.deleteDirectory(temp);
        return result;
      } catch (UnsupportedOperationException e) {
        throw new SmallerException("Operation is not available in setup");
      }
    }
  }

  private List<String> getOutputFiles(final File folder, final File root) {
    List<String> files = new ArrayList<String>();
    for (File file : folder.listFiles()) {
      if (file.isDirectory()) {
        files.addAll(getOutputFiles(file, root));
      } else {
        files.add(file.getAbsolutePath().substring(root.getAbsolutePath().length() + 1));
      }
    }
    return files;
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
   * 
   */
  public void dispose() {
    if (this.process != null) {
      this.process.destroy();
      this.process = null;
    }
    cleanupBinary();
  }

  private static class TempResourceResolver implements ResourceResolver {

    private final String root;

    TempResourceResolver(final String root) {
      this.root = root;
    }

    /**
     * @see de.matrixweb.smaller.resource.ResourceResolver#resolve(java.lang.String)
     */
    @Override
    public Resource resolve(final String path) {
      File file = new File(this.root, path);
      if (file.exists()) {
        try {
          return new StringResource(this, ResourceUtil.getType(path), path, FileUtils.readFileToString(file, "UTF-8"));
        } catch (IOException e) {
          throw new SmallerException("Failed to create resource from " + file.getAbsolutePath(), e);
        }
      }
      throw new SmallerException("Failed to create non existing resource from " + file.getAbsolutePath());
    }

    /**
     * @see de.matrixweb.smaller.resource.ResourceResolver#writeAll()
     */
    @Override
    public File writeAll() throws IOException {
      throw new UnsupportedOperationException();
    }

  }

}
