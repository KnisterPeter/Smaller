package com.sinnerschrader.smaller.clients.maven;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
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
   * A specific <code>fileSet</code> rule to select files and directories.
   * 
   * @parameter
   */
  private FileSet files;

  /**
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      OutputStream out = createZip();
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to create directory structure", e);
    }
  }

  private OutputStream createZip() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    FileSetManager fileSetManager = new FileSetManager();
    String[] includedFiles = fileSetManager.getIncludedFiles(files);
    File temp = File.createTempFile("maven-smaller", ".dir");
    temp.delete();
    temp.mkdirs();
    try {
      for (String includedFile : includedFiles) {
        getLog().debug("Adding " + includedFile + " to zip");
        File target = new File(temp, includedFile);
        target.getParentFile().mkdirs();
        FileUtils.copyFile(new File(includedFile), target);
      }
      Zip.zip(baos, temp);
    } finally {
      FileUtils.deleteDirectory(temp);
    }

    return baos;
  }

}
