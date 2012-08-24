package de.matrixweb.smaller.clients.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import de.matrixweb.smaller.clients.common.ExecutionException;
import de.matrixweb.smaller.clients.common.Logger;
import de.matrixweb.smaller.clients.common.Util;

/**
 * @author marwol
 * @goal smaller
 * @phase process-resources
 */
public class SmallerMojo extends AbstractMojo {

  /**
   * @parameter
   */
  private String processor;

  /**
   * @parameter
   */
  private String in;

  /**
   * @parameter
   */
  private String out;

  /**
   * The task options.
   * 
   * @parameter default-value=""
   */
  private String options;

  /**
   * The server host to connect to.
   * 
   * @parameter default-value="sr.s2.de"
   */
  private String host;

  /**
   * The server port to connect to.
   * 
   * @parameter default-value="80"
   */
  private String port;

  /**
   * A specific <code>fileSet</code> rule to select files and directories.
   * 
   * @parameter
   */
  private FileSet files;

  /**
   * The target folder.
   * 
   * @parameter
   */
  private File target;

  /**
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      final Util util = new Util(new MavenLogger());

      final File base = new File(this.files.getDirectory());
      final FileSetManager fileSetManager = new FileSetManager();
      final String[] includedFiles = fileSetManager
          .getIncludedFiles(this.files);

      util.unzip(this.target, util.send(this.host, this.port, util.zip(base,
          includedFiles, this.processor, this.in, this.out, this.options)));
    } catch (final ExecutionException e) {
      throw new MojoExecutionException("Failed execute smaller", e);
    }
  }

  private class MavenLogger implements Logger {

    /**
     * @see de.matrixweb.smaller.clients.common.Logger#debug(java.lang.String)
     */
    @Override
    public void debug(final String message) {
      getLog().debug(message);
    }

  }

}
