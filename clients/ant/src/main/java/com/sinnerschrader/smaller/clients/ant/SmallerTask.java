package com.sinnerschrader.smaller.clients.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.sinnerschrader.smaller.clients.common.ExecutionException;
import com.sinnerschrader.smaller.clients.common.Logger;
import com.sinnerschrader.smaller.clients.common.Util;

/**
 * @author marwol
 */
public class SmallerTask extends Task {

  private String processor;

  private String in;

  private String out;

  private String options = "";

  private String host = "sr.s2.de";

  private String port = "80";

  private FileSet files;

  private File target;

  private boolean debug = false;

  /**
   * @param processor
   *          the processor to set
   */
  public final void setProcessor(String processor) {
    this.processor = processor;
  }

  /**
   * @param in
   *          the in to set
   */
  public final void setIn(String in) {
    this.in = in;
  }

  /**
   * @param out
   *          the out to set
   */
  public final void setOut(String out) {
    this.out = out;
  }

  /**
   * @param options
   *          the options to set
   */
  public void setOptions(String options) {
    this.options = options;
  }

  /**
   * @param host
   *          the host to set
   */
  public final void setHost(String host) {
    this.host = host;
  }

  /**
   * @param port
   *          the port to set
   */
  public final void setPort(String port) {
    this.port = port;
  }

  /**
   * @param files
   *          the files to set
   */
  public final void setFiles(FileSet files) {
    this.files = files;
  }

  /**
   * @param files
   */
  public final void addFileset(FileSet files) {
    if (this.files != null) {
      throw new BuildException("Only one fileset is allowed");
    }
    this.files = files;
  }

  /**
   * @param target
   *          the target to set
   */
  public final void setTarget(File target) {
    this.target = target;
  }

  /**
   * @param debug
   *          the debug to set
   */
  public final void setDebug(boolean debug) {
    this.debug = debug;
  }

  /**
   * @see org.apache.tools.ant.Task#execute()
   */
  @Override
  public void execute() {
    try {
      Util util = new Util(new AntLogger(), debug);

      DirectoryScanner ds = files.getDirectoryScanner();
      util.unzip(target, util.send(host, port, util.zip(ds.getBasedir(), ds.getIncludedFiles(), processor, in, out, options)));
    } catch (ExecutionException e) {
      throw new BuildException("Failed execute smaller", e);
    }
  }

  private class AntLogger implements Logger {

    /**
     * @see com.sinnerschrader.smaller.clients.common.Logger#debug(java.lang.String)
     */
    @Override
    public void debug(String message) {
      log(message, Project.MSG_INFO);
    }

  }

}
