package de.matrixweb.smaller.client.osgi.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

  private static final Object LOCK = new Object();

  private final VFS vfs;

  private final Pipeline pipeline;

  private final ProcessDescription processDescription;

  private String buildResult;
  
  private String hash;

  /**
   * @param vfs
   * @param pipeline
   * @param processDescription
   * @throws IOException
   */
  public Servlet(final VFS vfs, final Pipeline pipeline,
      final ProcessDescription processDescription) throws IOException {
    this.vfs = vfs;
    this.pipeline = pipeline;
    this.processDescription = processDescription;
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
        this.pipeline.execute(Version.getCurrentVersion(), this.vfs,
            new VFSResourceResolver(this.vfs), null, this.processDescription);
        this.buildResult = VFSUtils.readToString(this.vfs
            .find(this.processDescription.getOutputFile()));
        this.hash = createVersionHash(buildResult);
      }
    }
  }

  private static String createVersionHash(String code) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(code.getBytes("UTF-8"));
      StringBuilder hexString = new StringBuilder();
      for (int i = 0; i < hash.length; i++) {
        String hex = Integer.toHexString(0xff & hash[i]);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new SmallerException("Failed to create version-hash", e);
    } catch (UnsupportedEncodingException e) {
      throw new SmallerException("Failed to create version-hash", e);
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
