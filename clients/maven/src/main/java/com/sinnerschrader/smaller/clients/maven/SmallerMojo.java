package com.sinnerschrader.smaller.clients.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import com.sinnerschrader.smaller.clients.common.ExecutionException;
import com.sinnerschrader.smaller.clients.common.Logger;
import com.sinnerschrader.smaller.clients.common.Util;

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
   * The server host to connect to.
   * 
   * @parameter default-value="sr.s2.de"
   */
  private String host;

  /**
   * The server port to connect to.
   * 
   * @parameter default-value="1148"
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
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      Util util = new Util(new MavenLogger());

      File base = new File(files.getDirectory());
      FileSetManager fileSetManager = new FileSetManager();
      String[] includedFiles = fileSetManager.getIncludedFiles(files);

      util.unzip(target, util.send(host, port, util.zip(base, includedFiles, processor, in, out)));
    } catch (ExecutionException e) {
      throw new MojoExecutionException("Failed execute smaller", e);
    }
  }

  private class MavenLogger implements Logger {

    /**
     * @see com.sinnerschrader.smaller.clients.common.Logger#debug(java.lang.String)
     */
    @Override
    public void debug(String message) {
      getLog().debug(message);
    }

  }

}
