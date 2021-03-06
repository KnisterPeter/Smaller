package de.matrixweb.smaller.client.osgi.internal;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.matrixweb.smaller.client.osgi.HashGenerator;
import de.matrixweb.smaller.common.ProcessDescription;
import de.matrixweb.smaller.common.SmallerException;
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
  
  private static final Logger LOGGER = LoggerFactory.getLogger(Servlet.class);

  private static final Object LOCK = new Object();

  private final VFS vfs;

  private final Pipeline pipeline;

  private final ProcessDescription processDescription;

  private String buildResult;
  
  private HashGenerator hashGenerator;
  
  private String hash;

  /**
   * @param vfs
   * @param pipeline
   * @param processDescription
   * @throws IOException
   */
  public Servlet(final VFS vfs, final Pipeline pipeline,
      final ProcessDescription processDescription) throws IOException {
    this(vfs, pipeline, processDescription, new SourceHashGenerator());
  }

  /**
   * @param vfs
   * @param pipeline
   * @param processDescription
   * @param hashGenerator
   * @throws IOException
   */
  public Servlet(final VFS vfs, final Pipeline pipeline,
      final ProcessDescription processDescription, HashGenerator hashGenerator) throws IOException {
    this.vfs = vfs;
    this.pipeline = pipeline;
    this.processDescription = processDescription;
    this.hashGenerator = hashGenerator;
    // TODO: Make eager building configurable
    build();
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
        try {
          // Note: First create hash to not have generated files in VFS
          this.hash = hashGenerator.createVersionHash(vfs);
          this.pipeline.execute(Version.getCurrentVersion(), this.vfs,
              new VFSResourceResolver(this.vfs), null, this.processDescription);
          this.buildResult = VFSUtils.readToString(this.vfs
              .find(this.processDescription.getOutputFile()));
        } catch (SmallerException e) {
          LOGGER.error("Failed to create resource for '" + processDescription.getOutputFile() + "'", e);
        }
      }
    }
  }

  String getHash() {
    return hash;
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
