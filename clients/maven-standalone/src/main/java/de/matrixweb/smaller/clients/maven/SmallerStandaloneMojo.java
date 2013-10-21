package de.matrixweb.smaller.clients.maven;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.model.fileset.FileSet;

import de.matrixweb.smaller.clients.common.ExecutionException;
import de.matrixweb.smaller.common.Task.GlobalOptions;
import de.matrixweb.smaller.common.Version;
import de.matrixweb.smaller.pipeline.Pipeline;
import de.matrixweb.smaller.pipeline.Result;
import de.matrixweb.smaller.resource.ProcessorFactory;
import de.matrixweb.smaller.resource.Type;
import de.matrixweb.smaller.resource.impl.JavaEEProcessorFactory;
import de.matrixweb.smaller.resource.vfs.VFS;
import de.matrixweb.smaller.resource.vfs.VFSResourceResolver;
import de.matrixweb.smaller.resource.vfs.wrapped.JavaFile;

/**
 * @author marwol
 * @goal smaller
 * @phase process-resources
 */
public class SmallerStandaloneMojo extends AbstractMojo {

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
   * @param target
   *          the target to set
   */
  public void setTarget(final String target) {
    this.target = new File(target);
  }

  /**
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("Write result to " + this.target);
    final SmallerClient client = new SmallerClient(getLog(), this.host,
        this.port, this.proxyhost, this.proxyport, this.target, this.processor,
        this.in, this.out, this.options, this.files, this.tasks) {
      /**
       * @see de.matrixweb.smaller.clients.maven.SmallerClient#executeSmaller(java.io.File,
       *      java.lang.String[], java.io.File, java.lang.String,
       *      java.lang.String, java.lang.String, java.lang.String,
       *      de.matrixweb.smaller.common.Task[])
       */
      @Override
      protected void executeSmaller(final File base,
          final String[] includedFiles, final File target, final String host,
          final String port, final String proxyhost, final String proxyport,
          final de.matrixweb.smaller.common.Task[] tasks)
          throws ExecutionException {
        final ProcessorFactory processorFactory = new JavaEEProcessorFactory();
        try {
          final VFS vfs = new VFS();
          try {
            vfs.mount(vfs.find("/"), new JavaFile(base));
            final Result result = new Pipeline(processorFactory).execute(
                Version.getCurrentVersion(), vfs, new VFSResourceResolver(vfs),
                tasks[0]);
            if (!GlobalOptions.isOutOnly(tasks[0])) {
              vfs.exportFS(target);
            }
            for (final String out : tasks[0].getOut()) {
              for (final Type type : Type.values()) {
                if (type.isOfType(FilenameUtils.getExtension(out))) {
                  FileUtils.writeStringToFile(new File(target, out), result
                      .get(type).getContents());
                }
              }
            }
          } finally {
            vfs.dispose();
          }
        } catch (final IOException e) {
          throw new ExecutionException("Embedded smaller failed", e);
        } finally {
          processorFactory.dispose();
        }
      }
    };
    client.execute();
  }

}
