package de.matrixweb.smaller.clients.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.codehaus.jackson.map.ObjectMapper;

import de.matrixweb.smaller.common.Manifest;
import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.common.Task;
import de.matrixweb.smaller.common.Zip;

/**
 * @author marwol
 */
public class Util {

  private final Logger logger;

  private boolean debug = false;

  /**
   * @param logger
   */
  public Util(final Logger logger) {
    this(logger, false);
  }

  /**
   * @param logger
   * @param debug
   */
  public Util(final Logger logger, final boolean debug) {
    this.logger = logger;
    this.debug = debug;
  }

  /**
   * @param base
   * @param includedFiles
   * @param processor
   * @param in
   * @param out
   * @return Returns the zipped file as byte[]
   * @throws ExecutionException
   */
  public byte[] zip(final File base, final String[] includedFiles,
      final String processor, final String in, final String out)
      throws ExecutionException {
    return zip(base, includedFiles, processor, in, out, "");
  }

  /**
   * @param base
   * @param includedFiles
   * @param processor
   * @param in
   * @param out
   * @param options
   * @return Returns the zipped file as byte[]
   * @throws ExecutionException
   */
  public byte[] zip(final File base, final String[] includedFiles,
      final String processor, final String in, final String out,
      final String options) throws ExecutionException {
    return zip(base, includedFiles,
        new Task[] { createTask(processor, in, out, options) });
  }

  /**
   * @param base
   * @param includedFiles
   * @param tasks
   * @return Returns the zipped file as byte[]
   * @throws ExecutionException
   */
  public byte[] zip(final File base, final String[] includedFiles,
      final Task[] tasks) throws ExecutionException {
    try {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();

      final File temp = File.createTempFile("maven-smaller", ".dir");
      temp.delete();
      temp.mkdirs();
      final Manifest manifest = writeManifest(temp, tasks);
      try {
        for (final String includedFile : includedFiles) {
          this.logger.debug("Adding " + includedFile + " to zip");
          final File target = new File(temp, includedFile);
          target.getParentFile().mkdirs();
          FileUtils.copyFile(new File(base, includedFile), target);
        }
        for (final String included : manifest.getTasks()[0].getIn()) {
          final File target = new File(temp, included);
          target.getParentFile().mkdirs();
          FileUtils.copyFile(new File(base, included), target);
        }
        Zip.zip(baos, temp);
      } finally {
        if (!this.debug) {
          FileUtils.deleteDirectory(temp);
        } else {
          this.logger.debug("Path to input files: " + temp);
        }
      }

      return baos.toByteArray();
    } catch (final IOException e) {
      throw new ExecutionException("Failed to create zip file for upload", e);
    }
  }

  private Task createTask(final String processor, final String in,
      final String out, final String options) {
    return new Task(processor, in, out, options);
  }

  private Manifest writeManifest(final File temp, final Task[] task)
      throws ExecutionException {
    try {
      final Manifest manifest = new Manifest(task);
      final File metaInf = new File(temp, "META-INF");
      metaInf.mkdirs();
      new ObjectMapper().writeValue(new File(metaInf, "MAIN.json"), manifest);
      return manifest;
    } catch (final IOException e) {
      throw new ExecutionException("Failed to write manifest", e);
    }
  }

  /**
   * @param host
   * @param port
   * @param bytes
   * @return the response as {@link InputStream}
   * @throws ExecutionException
   */
  public byte[] send(final String host, final String port, final byte[] bytes)
      throws ExecutionException {
    return send(host, port, null, null, bytes);
  }

  /**
   * @param host
   * @param port
   * @param proxyhost
   * @param proxyport
   * @param bytes
   * @return the response as {@link InputStream}
   * @throws ExecutionException
   */
  public byte[] send(final String host, final String port,
      final String proxyhost, final String proxyport, final byte[] bytes)
      throws ExecutionException {
    try {
      final Request request = Request.Post("http://" + host + ":" + port)
          .socketTimeout(0).connectTimeout(0);
      if (proxyhost != null && proxyport != null) {
        request.viaProxy(new HttpHost(proxyhost, Integer.valueOf(proxyport)));
      }
      final HttpResponse response = request.bodyByteArray(bytes).execute()
          .returnResponse();
      return handleResponse(response);
    } catch (final Exception e) {
      throw new ExecutionException("Failed to send zip file", e);
    }
  }

  private byte[] handleResponse(final HttpResponse response)
      throws IOException, ExecutionException {
    final InputStream in = response.getEntity().getContent();
    try {
      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        throw new ExecutionException(IOUtils.toString(in));
      }
      if (getHeader(response, "X-Smaller-Status").equals("ERROR")) {
        throw new SmallerException("Server Error: "
            + getHeader(response, "X-Smaller-Message"));
      }
      return IOUtils.toByteArray(in);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  private String getHeader(final HttpResponse response, final String name) {
    final Header header = response.getFirstHeader(name);
    return header != null ? header.getValue() : "";
  }

  /**
   * @param target
   * @param bytes
   * @throws ExecutionException
   */
  public void unzip(final File target, final byte[] bytes)
      throws ExecutionException {
    try {
      final File temp = File.createTempFile("smaller", ".zip");
      temp.delete();
      final FileOutputStream fos = new FileOutputStream(temp);
      try {
        IOUtils.write(bytes, fos);

        target.mkdirs();
        Zip.unzip(temp, target);
      } finally {
        IOUtils.closeQuietly(fos);
        if (!this.debug) {
          temp.delete();
        } else {
          this.logger.debug("Path to output files: " + temp);
        }
      }
    } catch (final IOException e) {
      throw new ExecutionException("Failed to handle smaller response", e);
    }
  }

}
