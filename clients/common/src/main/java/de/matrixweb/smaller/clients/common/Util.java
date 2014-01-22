package de.matrixweb.smaller.clients.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.codehaus.jackson.map.ObjectMapper;

import de.matrixweb.smaller.common.Manifest;
import de.matrixweb.smaller.common.ProcessDescription;
import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.common.Version;
import de.matrixweb.smaller.common.Zip;
import de.matrixweb.smaller.config.ConfigFile;
import de.matrixweb.smaller.config.Environment;
import de.matrixweb.smaller.config.Processor;

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
   * @param configFile
   * @return Returns the zipped file as byte[]
   * @throws ExecutionException
   */
  public byte[] zip(final File base, final List<String> includedFiles,
      final ConfigFile configFile) throws ExecutionException {
    try {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();

      final File temp = File.createTempFile("maven-smaller", ".dir");
      try {
        temp.delete();
        temp.mkdirs();
        final Manifest manifest = writeManifest(temp, configFile);

        for (final String includedFile : includedFiles) {
          this.logger.debug("Adding " + includedFile + " to zip");
          final File target = new File(temp, includedFile);
          target.getParentFile().mkdirs();
          FileUtils.copyFile(new File(base, includedFile), target);
        }
        for (final ProcessDescription pd : manifest.getProcessDescriptions()) {
          final File target = new File(temp, pd.getInputFile());
          target.getParentFile().mkdirs();
          FileUtils.copyFile(new File(base, pd.getInputFile()), target);
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

  private Manifest writeManifest(final File temp, final ConfigFile configFile)
      throws ExecutionException {
    try {
      final Manifest manifest = convertConfigFileToManifest(configFile);
      final File metaInf = new File(temp, "META-INF");
      metaInf.mkdirs();
      new ObjectMapper()
          .writeValue(new File(metaInf, "smaller.json"), manifest);

      return manifest;
    } catch (final IOException e) {
      throw new ExecutionException("Failed to write manifest", e);
    }
  }

  /**
   * @param configFile
   * @return Returns a new {@link Manifest}
   */
  public Manifest convertConfigFileToManifest(final ConfigFile configFile) {
    final Manifest manifest = new Manifest();
    for (final Environment env : configFile.getEnvironments().values()) {
      final ProcessDescription processDescription = new ProcessDescription();
      processDescription.setInputFile(env.getProcessors()
          .get(env.getPipeline()[0]).getSrc());
      processDescription.setOutputFile(env.getProcess()[0]);
      for (final String name : env.getPipeline()) {
        final de.matrixweb.smaller.common.ProcessDescription.Processor processor = new de.matrixweb.smaller.common.ProcessDescription.Processor();
        processor.setName(name);
        final Processor p = env.getProcessors().get(name);
        if (p != null) {
          processor.getOptions().putAll(p.getPlainOptions());
        }
        processDescription.getProcessors().add(processor);
      }

      manifest.getProcessDescriptions().add(processDescription);
    }
    return manifest;
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
      final HttpResponse response = request
          .addHeader(Version.HEADER, Version.getCurrentVersion().toString())
          .bodyByteArray(bytes).execute().returnResponse();
      return handleResponse(response);
    } catch (final Exception e) {
      if (e instanceof SmallerException) {
        throw (SmallerException) e;
      }
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
        throw new SmallerException(getHeader(response, "X-Smaller-Message")
            .replace("#@@#", "\n"));
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

  /**
   * Formats the given exception in a multiline error message with all causes.
   * 
   * @param e
   *          Exception for format as error message
   * @return Returns an error string
   */
  public static String formatException(final Exception e) {
    final StringBuilder sb = new StringBuilder();
    Throwable t = e;
    while (t != null) {
      sb.append(t.getMessage()).append("\n");
      t = t.getCause();
    }
    return sb.toString();
  }

}
