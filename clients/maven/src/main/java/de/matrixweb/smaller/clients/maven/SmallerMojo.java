package de.matrixweb.smaller.clients.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

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
   * @parameter alias="config-file" default-value="${basedir}/smaller.yml"
   */
  private File configFile;

  /**
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    new SmallerClient(getLog(), this.host, this.port, this.proxyhost,
        this.proxyport, this.target, this.configFile).execute();
  }

}
