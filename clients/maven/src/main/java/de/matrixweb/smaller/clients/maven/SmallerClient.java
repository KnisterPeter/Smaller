package de.matrixweb.smaller.clients.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import de.matrixweb.smaller.clients.common.ExecutionException;
import de.matrixweb.smaller.clients.common.Logger;
import de.matrixweb.smaller.clients.common.Util;

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

  private final String processor;

  private final String in;

  private final String out;

  private final String options;

  private final FileSet files;

  private List<Task> tasks;

  /**
   * @param log
   * @param host
   * @param port
   * @param proxyhost
   * @param proxyport
   * @param target
   * @param processor
   * @param in
   * @param out
   * @param options
   * @param files
   * @param tasks
   */
  public SmallerClient(final Log log, final String host, final String port,
      final String proxyhost, final String proxyport, final File target,
      final String processor, final String in, final String out,
      final String options, final FileSet files, final List<Task> tasks) {
    super();
    this.log = log;
    this.host = host;
    this.port = port;
    this.proxyhost = proxyhost;
    this.proxyport = proxyport;
    this.target = target;
    this.processor = processor;
    this.in = in;
    this.out = out;
    this.options = options;
    this.files = files;
    this.tasks = tasks;
  }

  /**
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
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

      executeSmaller(base, includedFiles, this.target, this.host, this.port,
          this.proxyhost, this.proxyport, convertTasks());
    } catch (final ExecutionException e) {
      this.log.error(Util.formatException(e));
      throw new MojoExecutionException("Failed execute smaller", e);
    }
  }

  protected void executeSmaller(final File base, final String[] includedFiles,
      final File target, final String host, final String port,
      final String proxyhost, final String proxyport,
      final de.matrixweb.smaller.common.Task[] tasks) throws ExecutionException {
    final Util util = new Util(new MavenLogger());
    util.unzip(
        target,
        util.send(host, port, proxyhost, proxyport,
            util.zip(base, includedFiles, tasks)));
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
      SmallerClient.this.log.debug(message);
    }

  }

}
