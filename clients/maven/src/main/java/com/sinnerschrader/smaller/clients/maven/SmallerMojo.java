package com.sinnerschrader.smaller.clients.maven;

import java.util.Arrays;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

/**
 * @author marwol
 * @goal smaller
 * @phase process-resources
 */
public class SmallerMojo extends AbstractMojo {

  /**
   * A specific <code>fileSet</code> rule to select files and directories.
   * 
   * @parameter
   */
  private FileSet fileset;

  /**
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    FileSetManager fileSetManager = new FileSetManager();
    String[] includedFiles = fileSetManager.getIncludedFiles(fileset);
    getLog().info(Arrays.toString(includedFiles));
  }

}
