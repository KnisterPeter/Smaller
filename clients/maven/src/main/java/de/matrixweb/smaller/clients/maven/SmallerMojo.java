package de.matrixweb.smaller.clients.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    try {
      final Util util = new Util(new MavenLogger());

      final File base = new File(this.files.getDirectory());
      final FileSetManager fileSetManager = new FileSetManager();
      final String[] includedFiles = fileSetManager
          .getIncludedFiles(this.files);

      if (this.processor != null && this.in != null && this.out != null) {
        final Task direct = new Task();
        direct.setProcessor(this.processor);
        direct.setIn(this.in);
        direct.setOut(this.out);
        direct.setOptions(this.options);
        if (this.tasks == null) {
          this.tasks = new ArrayList<Task>();
        }
        this.tasks.add(direct);
      }

      util.unzip(
          this.target,
          util.send(this.host, this.port,
              util.zip(base, includedFiles, convertTasks())));
    } catch (final ExecutionException e) {
      throw new MojoExecutionException("Failed execute smaller", e);
    }
  }

  private de.matrixweb.smaller.common.Task[] convertTasks() {
    final List<de.matrixweb.smaller.common.Task> list = new ArrayList<de.matrixweb.smaller.common.Task>();
    for (final Task task : this.tasks) {
      list.add(new de.matrixweb.smaller.common.Task(task.getProcessor(), task
          .getIn(), task.getOut(), task.getOptions()));
    }
    return list.toArray(new de.matrixweb.smaller.common.Task[list.size()]);
  }

  private class MavenLogger implements Logger {

    /**
     * @see de.matrixweb.smaller.clients.common.Logger#debug(java.lang.String)
     */
    public void debug(final String message) {
      getLog().debug(message);
    }

  }

}
