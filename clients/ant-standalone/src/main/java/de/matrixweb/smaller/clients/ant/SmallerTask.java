package de.matrixweb.smaller.clients.ant;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import de.matrixweb.smaller.clients.common.Util;
import de.matrixweb.smaller.pipeline.Pipeline;
import de.matrixweb.smaller.pipeline.Result;
import de.matrixweb.smaller.resource.FileResourceResolver;
import de.matrixweb.smaller.resource.Type;
import de.matrixweb.smaller.resource.impl.JavaEEProcessorFactory;

/**
 * @author marwol
 */
public class SmallerTask extends Task {

  private String processor;

  private String in;

  private String out;

  private String options = "";

  private FileSet files;

  private File target;

  /**
   * @param processor
   *          the processor to set
   */
  public final void setProcessor(final String processor) {
    this.processor = processor;
  }

  /**
   * @param in
   *          the in to set
   */
  public final void setIn(final String in) {
    this.in = in;
  }

  /**
   * @param out
   *          the out to set
   */
  public final void setOut(final String out) {
    this.out = out;
  }

  /**
   * @param options
   *          the options to set
   */
  public void setOptions(final String options) {
    this.options = options;
  }

  /**
   * @param host
   *          the host to set
   */
  public final void setHost(final String host) {
  }

  /**
   * @param port
   *          the port to set
   */
  public final void setPort(final String port) {
  }

  /**
   * @param proxyhost
   *          the proxyhost to set
   */
  public void setProxyhost(final String proxyhost) {
  }

  /**
   * @param proxyport
   *          the proxyport to set
   */
  public void setProxyport(final String proxyport) {
  }

  /**
   * @param files
   *          the files to set
   */
  public final void setFiles(final FileSet files) {
    this.files = files;
  }

  /**
   * @param files
   */
  public final void addFileset(final FileSet files) {
    if (this.files != null) {
      throw new BuildException("Only one fileset is allowed");
    }
    this.files = files;
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
  }

  /**
   * @see org.apache.tools.ant.Task#execute()
   */
  @Override
  public void execute() {
    try {
      final DirectoryScanner ds = this.files.getDirectoryScanner();
      de.matrixweb.smaller.common.Task task = new de.matrixweb.smaller.common.Task(this.processor, this.in, this.out, this.options);
      final Result result = new Pipeline(new JavaEEProcessorFactory()).execute(new FileResourceResolver(ds.getBasedir().getAbsolutePath()), task);

      for (final String out : task.getOut()) {
        for (final Type type : Type.values()) {
          if (type.isOfType(FilenameUtils.getExtension(out))) {
            FileUtils.writeStringToFile(new File(this.target, out), result.get(type).getContents());
          }
        }
      }
    } catch (final IOException e) {
      log(Util.formatException(e), Project.MSG_ERR);
      throw new BuildException("Failed execute embedded smaller", e);
    }
  }

}
