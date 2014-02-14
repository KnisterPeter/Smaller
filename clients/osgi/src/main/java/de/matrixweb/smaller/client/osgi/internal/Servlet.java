package de.matrixweb.smaller.client.osgi.internal;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.matrixweb.smaller.common.ProcessDescription;
import de.matrixweb.smaller.common.Version;
import de.matrixweb.smaller.pipeline.Pipeline;
import de.matrixweb.smaller.resource.VFSResourceResolver;
import de.matrixweb.vfs.VFS;
import de.matrixweb.vfs.VFSUtils;

/**
 * @author markusw
 */
public class Servlet extends HttpServlet {

  private static final long serialVersionUID = 2386876386135939230L;

  private static final Object LOCK = new Object();

  private final VFS vfs;

  private final Pipeline pipeline;

  private final ProcessDescription processDescription;

  private String buildResult;

  /**
   * @param vfs
   * @param pipeline
   * @param processDescription
   */
  public Servlet(final VFS vfs, final Pipeline pipeline,
      final ProcessDescription processDescription) {
    this.vfs = vfs;
    this.pipeline = pipeline;
    this.processDescription = processDescription;
  }

  /**
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void service(final HttpServletRequest request,
      final HttpServletResponse response) throws ServletException, IOException {
    // TODO: Invalidate result when bundles update occurs
    if (this.buildResult == null) {
      build();
    }

    response.setContentType(getContentType(request));
    final PrintWriter writer = response.getWriter();
    writer.print(this.buildResult);
    writer.close();
  }

  private void build() throws IOException {
    synchronized (LOCK) {
      if (this.buildResult == null) {
        this.pipeline.execute(Version.getCurrentVersion(), this.vfs,
            new VFSResourceResolver(this.vfs), null, this.processDescription);
        this.buildResult = VFSUtils.readToString(this.vfs
            .find(this.processDescription.getOutputFile()));
      }
    }
  }

  private String getContentType(final HttpServletRequest request) {
    String contentType = request.getContentType();
    if (contentType == null) {
      if (request.getRequestURI().endsWith("js")) {
        contentType = "text/javascript";
      } else if (request.getRequestURI().endsWith("css")) {
        contentType = "text/css";
      }
    }
    return contentType;
  }

}
