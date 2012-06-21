package com.sinnerschrader.smaller.clients.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.fluent.Request;
import org.codehaus.jackson.map.ObjectMapper;

import com.sinnerschrader.smaller.common.Manifest;
import com.sinnerschrader.smaller.common.Manifest.Task;
import com.sinnerschrader.smaller.common.Manifest.Task.Options;
import com.sinnerschrader.smaller.common.Zip;

/**
 * @author marwol
 */
public class Util {

  private Logger logger;

  private boolean debug = false;

  /**
   * @param logger
   */
  public Util(Logger logger) {
    this(logger, false);
  }

  /**
   * @param logger
   * @param debug
   */
  public Util(Logger logger, boolean debug) {
    this.logger = logger;
    this.debug = debug;
  }

  /**
   * @param base
   * @param includedFiles
   * @param processor
   * @param in
   * @param out
   * @return the zipped file as byte[]
   * @throws ExecutionException
   */
  public byte[] zip(File base, String[] includedFiles, String processor, String in, String out) throws ExecutionException {
    return zip(base, includedFiles, processor, in, out, "");
  }

  /**
   * @param base
   * @param includedFiles
   * @param processor
   * @param in
   * @param out
   * @param options
   * @return the zipped file as byte[]
   * @throws ExecutionException
   */
  public byte[] zip(File base, String[] includedFiles, String processor, String in, String out, String options) throws ExecutionException {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      File temp = File.createTempFile("maven-smaller", ".dir");
      temp.delete();
      temp.mkdirs();
      Manifest manifest = writeManifest(temp, processor, in, out, options);
      try {
        for (String includedFile : includedFiles) {
          logger.debug("Adding " + includedFile + " to zip");
          File target = new File(temp, includedFile);
          target.getParentFile().mkdirs();
          FileUtils.copyFile(new File(base, includedFile), target);
        }
        for (String included : manifest.getTasks()[0].getIn()) {
          File target = new File(temp, included);
          target.getParentFile().mkdirs();
          FileUtils.copyFile(new File(base, included), target);
        }
        Zip.zip(baos, temp);
      } finally {
        if (!debug) {
          FileUtils.deleteDirectory(temp);
        } else {
          logger.debug("Path to input files: " + temp);
        }
      }

      return baos.toByteArray();
    } catch (IOException e) {
      throw new ExecutionException("Failed to create zip file for upload", e);
    }
  }

  private Manifest writeManifest(File temp, String processor, String in, String out, String options) throws ExecutionException {
    try {
      EnumSet<Options> set = EnumSet.noneOf(Options.class);
      if (options != null && !"".equals(options)) {
        for (String option : options.split(",")) {
          set.add(Options.valueOf(option.toUpperCase().replace('-', '_')));
        }
      }
      Task task = new Task(processor, in, out);
      task.setOptions(set);
      Manifest manifest = new Manifest(task);
      File metaInf = new File(temp, "META-INF");
      metaInf.mkdirs();
      new ObjectMapper().writeValue(new File(metaInf, "MAIN.json"), manifest);
      return manifest;
    } catch (IOException e) {
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
  public byte[] send(String host, String port, byte[] bytes) throws ExecutionException {
    try {
      return Request.Post("http://" + host + ":" + port).bodyByteArray(bytes).execute().returnContent().asBytes();
    } catch (Exception e) {
      throw new ExecutionException("Failed to send zip file", e);
    }
  }

  /**
   * @param target
   * @param bytes
   * @throws ExecutionException
   */
  public void unzip(File target, byte[] bytes) throws ExecutionException {
    try {
      File temp = File.createTempFile("smaller", ".zip");
      temp.delete();
      FileOutputStream fos = new FileOutputStream(temp);
      try {
        IOUtils.write(bytes, fos);

        target.mkdirs();
        Zip.unzip(temp, target);
      } finally {
        IOUtils.closeQuietly(fos);
        if (!debug) {
          temp.delete();
        } else {
          logger.debug("Path to output files: " + temp);
        }
      }
    } catch (IOException e) {
      throw new ExecutionException("Failed to handle smaller response", e);
    }
  }

}
