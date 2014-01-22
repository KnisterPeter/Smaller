package de.matrixweb.smaller.clients.ant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import de.matrixweb.smaller.clients.common.ExecutionException;
import de.matrixweb.smaller.clients.common.Logger;
import de.matrixweb.smaller.clients.common.Util;
import de.matrixweb.smaller.config.ConfigFile;
import de.matrixweb.smaller.config.Environment;

/**
 * @author marwol
 */
public class SmallerTask extends Task {

  private String host = "sr.s2.de";

  private String port = "80";

  private String proxyhost = null;

  private String proxyport = null;

  private File target;

  private boolean debug = false;

  private File configFilePath;

  /**
   * @param host
   *          the host to set
   */
  public final void setHost(final String host) {
    this.host = host;
  }

  /**
   * @param port
   *          the port to set
   */
  public final void setPort(final String port) {
    this.port = port;
  }

  /**
   * @param proxyhost
   *          the proxyhost to set
   */
  public void setProxyhost(final String proxyhost) {
    this.proxyhost = proxyhost;
  }

  /**
   * @param proxyport
   *          the proxyport to set
   */
  public void setProxyport(final String proxyport) {
    this.proxyport = proxyport;
  }

  /**
   * @param target
   *          the target to set
   */
  public final void setTarget(final File target) {
    this.target = target;
  }

  /**
   * @param debug
   *          the debug to set
   */
  public final void setDebug(final boolean debug) {
    this.debug = debug;
  }

  /**
   * @param configFile
   *          the configFile to set
   */
  public void setConfigFile(final File configFile) {
    this.configFilePath = configFile;
  }

  /**
   * @see org.apache.tools.ant.Task#execute()
   */
  @Override
  public void execute() {
    try {
      final Util util = new Util(new AntLogger(), this.debug);

      final File temp = File.createTempFile("smaller-ant", ".dir");
      try {
        temp.delete();
        temp.mkdirs();

        log("Reading config-file: " + this.configFilePath);
        if (!this.configFilePath.exists()) {
          throw new RuntimeException(this.configFilePath.toString());
        }
        final ConfigFile configFile = ConfigFile.read(this.configFilePath);

        final List<String> includedFiles = new ArrayList<String>();
        for (final Environment env : configFile.getEnvironments().values()) {
          for (final String dir : env.getFiles().getFolder()) {
            copyFirstInputFile(env, dir, temp);

            final String base = new File(this.configFilePath.getParentFile(),
                dir).getAbsolutePath();
            final String[] included = scanIncludedFiles(base, env.getFiles()
                .getIncludes(), env.getFiles().getExcludes());

            for (final String include : included) {
              FileUtils.copyFile(new File(base, include), new File(temp,
                  include));
              includedFiles.add(include);
            }
          }
        }

        util.unzip(this.target, util.send(this.host, this.port, this.proxyhost,
            this.proxyport, util.zip(temp, includedFiles, configFile)));
      } finally {
        FileUtils.deleteDirectory(temp);
      }
    } catch (final IOException e) {
      log(Util.formatException(e), Project.MSG_ERR);
      throw new BuildException("Failed execute smaller", e);
    } catch (final ExecutionException e) {
      log(Util.formatException(e), Project.MSG_ERR);
      throw new BuildException("Failed execute smaller", e);
    }
  }

  private void copyFirstInputFile(final Environment env, final String dir,
      final File temp) throws IOException {
    final String input = env.getProcessors().get(env.getPipeline()[0]).getSrc();
    final File inputFile = new File(new File(
        this.configFilePath.getParentFile(), dir), input);
    if (inputFile.exists()) {
      FileUtils.copyFile(inputFile, new File(temp, input));
    }
  }

  private String[] scanIncludedFiles(final String dir, final String[] includes,
      final String[] excludes) {
    final FileSet set = new FileSet();
    set.setProject(getProject());
    log("Scanning " + dir);
    set.setDir(new File(dir));
    set.appendIncludes(includes);
    set.appendExcludes(excludes);
    return set.getDirectoryScanner().getIncludedFiles();
  }

  private class AntLogger implements Logger {

    /**
     * @see de.matrixweb.smaller.clients.common.Logger#debug(java.lang.String)
     */
    public void debug(final String message) {
      log(message, Project.MSG_INFO);
    }

  }

}
