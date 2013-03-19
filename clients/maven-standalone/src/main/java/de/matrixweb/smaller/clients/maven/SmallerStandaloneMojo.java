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
import de.matrixweb.smaller.pipeline.Pipeline;
import de.matrixweb.smaller.pipeline.Result;
import de.matrixweb.smaller.resource.FileResourceResolver;
import de.matrixweb.smaller.resource.Type;
import de.matrixweb.smaller.resource.impl.JavaEEProcessorFactory;

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
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
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
        try {
          final Result result = new Pipeline(new JavaEEProcessorFactory())
              .execute(new FileResourceResolver(base.getAbsolutePath()),
                  tasks[0]);
          for (final String out : tasks[0].getOut()) {
            for (final Type type : Type.values()) {
              if (type.isOfType(FilenameUtils.getExtension(out))) {
                FileUtils.writeStringToFile(new File(target, out),
                    result.get(type).getContents());
              }
            }
          }
        } catch (final IOException e) {
          throw new ExecutionException("Embedded smaller failed", e);
        }
      }
    };
    client.execute();
  }

}
