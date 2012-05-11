package com.sinnerschrader.smaller.clients.maven;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import com.sinnerschrader.smaller.Zip;

/**
 * @author marwol
 * @goal smaller
 * @phase process-resources
 */
public class SmallerMojo extends AbstractMojo {

  /**
   * The server host to connect to.
   * 
   * @parameter default-value="sr.s2.de"
   */
  private String host;

  /**
   * The server port to connect to.
   * 
   * @parameter default-value="1148"
   */
  private String port;

  /**
   * A specific <code>fileSet</code> rule to select files and directories.
   * 
   * @parameter
   */
  private FileSet files;

  /**
   * The target folder.
   * 
   * @parameter
   */
  private File target;

  /**
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    unzip(send(zip()));
  }

  private OutputStream zip() throws MojoExecutionException {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      File base = new File(files.getDirectory());
      FileSetManager fileSetManager = new FileSetManager();
      files.getDirectory();
      String[] includedFiles = fileSetManager.getIncludedFiles(files);
      File temp = File.createTempFile("maven-smaller", ".dir");
      temp.delete();
      temp.mkdirs();
      try {
        for (String includedFile : includedFiles) {
          getLog().debug("Adding " + includedFile + " to zip");
          File target = new File(temp, includedFile);
          target.getParentFile().mkdirs();
          FileUtils.copyFile(new File(base, includedFile), target);
        }
        Zip.zip(baos, temp);
      } finally {
        FileUtils.deleteDirectory(temp);
      }

      return baos;
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to create zip file for upload", e);
    }
  }

  private InputStream send(OutputStream out) throws MojoExecutionException {
    try {
      CamelContext cc = new DefaultCamelContext();
      try {
        cc.start();
        InputStream in = cc.createProducerTemplate().requestBody("http4://" + host + ':' + port, out, InputStream.class);
        return in;
      } finally {
        cc.stop();
      }
    } catch (Exception e) {
      throw new MojoExecutionException("Failed to send zip file", e);
    }
  }

  private void unzip(InputStream in) throws MojoExecutionException {
    try {
      File temp = File.createTempFile("smaller", ".zip");
      temp.delete();
      FileOutputStream fos = new FileOutputStream(temp);
      try {
        IOUtils.copy(in, fos);

        target.mkdirs();
        Zip.unzip(temp, target);
      } finally {
        IOUtils.closeQuietly(fos);
        temp.delete();
      }
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to handle smaller response", e);
    }
  }

}
