package de.matrixweb.smaller.clients.maven;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.model.fileset.FileSet;

/**
 * @author marwol
 * @goal smaller
 * @phase process-resources
 */
public class SmallerMojo extends AbstractMojo {

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
   * The proxy host to connect to.
   */
  private final String proxyhost = null;

  /**
   * The proxy port to connect to.
   */
  private final String proxyport = null;

  /**
   * The target folder.
   * 
   * @parameter
   */
  private File target;

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
   * A specific <code>fileSet</code> rule to select files and directories.
   * 
   * @parameter
   */
  private FileSet files;

  /**
   * A definition of task to execute as once
   * 
   * @parameter
   */
  private List<Task> tasks;

  /**
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    new SmallerClient(getLog(), this.host, this.port, this.proxyhost,
        this.proxyport, this.target, this.processor, this.in, this.out,
        this.options, this.files, this.tasks).execute();
  }

}
