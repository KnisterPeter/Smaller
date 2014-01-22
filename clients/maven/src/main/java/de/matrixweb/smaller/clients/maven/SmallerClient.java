package de.matrixweb.smaller.clients.maven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import de.matrixweb.smaller.clients.common.ExecutionException;
import de.matrixweb.smaller.clients.common.Logger;
import de.matrixweb.smaller.clients.common.Util;
import de.matrixweb.smaller.config.ConfigFile;
import de.matrixweb.smaller.config.Environment;

/**
 * @author markusw
 */
public class SmallerClient {

  private final Log log;

  private final String host;

  private final String port;

  private final String proxyhost;

  private final String proxyport;

  private final File target;

  private final File configFilePath;

  /**
   * @param log
   * @param host
   * @param port
   * @param proxyhost
   * @param proxyport
   * @param target
   * @param configFilePath
   */
  public SmallerClient(final Log log, final String host, final String port,
      final String proxyhost, final String proxyport, final File target,
      final File configFilePath) {
    super();
    this.log = log;
    this.host = host;
    this.port = port;
    this.proxyhost = proxyhost;
    this.proxyport = proxyport;
    this.target = target;
    this.configFilePath = configFilePath;
  }

  /**
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      final File temp = File.createTempFile("smaller-maven", ".dir");
      try {
        temp.delete();
        temp.mkdirs();

        this.log.info("Reading config-file: " + this.configFilePath);
        final ConfigFile configFile = ConfigFile.read(this.configFilePath);

        final List<String> includedFiles = new ArrayList<String>();
        for (final Environment env : configFile.getEnvironments().values()) {
          for (final String dir : env.getFiles().getFolder()) {
            copyFirstInputFile(env, dir, temp);

            File base = new File(dir);
            if (!base.isAbsolute()) {
              base = new File(this.configFilePath.getParentFile(),
                  base.getPath());
            }
            final String[] included = scanIncludedFiles(base.getAbsolutePath(),
                env.getFiles().getIncludes(), env.getFiles().getExcludes());

            for (final String include : included) {
              FileUtils.copyFile(new File(base, include), new File(temp,
                  include));
              includedFiles.add(include);
            }
          }
        }

        executeSmaller(temp, includedFiles, this.target, this.host, this.port,
            this.proxyhost, this.proxyport, configFile);
      } catch (final ExecutionException e) {
        this.log.error(Util.formatException(e));
        throw new MojoExecutionException("Failed execute smaller", e);
      } finally {
        FileUtils.deleteDirectory(temp);
      }
    } catch (final IOException e) {
      throw new MojoExecutionException("Failed to read config file from "
          + this.configFilePath, e);
    }
  }

  private void copyFirstInputFile(final Environment env, final String dir,
      final File temp) throws IOException {
    final String input = env.getProcessors().get(env.getPipeline()[0]).getSrc();
    if (input != null) {
      File inputFile = new File(dir, input);
      if (!inputFile.isAbsolute()) {
        inputFile = new File(this.configFilePath.getParentFile(),
            inputFile.getPath());
      }

      if (inputFile.exists()) {
        FileUtils.copyFile(inputFile, new File(temp, input));
      }
    }
  }

  private String[] scanIncludedFiles(final String dir, final String[] includes,
      final String[] excludes) {
    final FileSet set = new FileSet();
    this.log.debug("Scanning " + dir);
    set.setDirectory(dir);
    if (includes != null) {
      for (final String include : includes) {
        this.log.debug("  including " + include);
        set.addInclude(include);
      }
    }
    if (excludes != null) {
      for (final String exclude : excludes) {
        this.log.debug("  excluding " + exclude);
        set.addExclude(exclude);
      }
    }
    return new FileSetManager().getIncludedFiles(set);
  }

  protected void executeSmaller(final File base,
      final List<String> includedFiles, final File target, final String host,
      final String port, final String proxyhost, final String proxyport,
      final ConfigFile configFile) throws ExecutionException {
    final Util util = new Util(new MavenLogger());
    util.unzip(
        target,
        util.send(host, port, proxyhost, proxyport,
            util.zip(base, includedFiles, configFile)));
  }

  private class MavenLogger implements Logger {

    /**
     * @see de.matrixweb.smaller.clients.common.Logger#debug(java.lang.String)
     */
    public void debug(final String message) {
      SmallerClient.this.log.debug(message);
    }

  }

}
