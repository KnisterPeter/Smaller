package de.matrixweb.smaller.common;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 * @author marwol
 */
public class ZipTest {

  /**
   * @throws URISyntaxException
   * @throws IOException
   */
  @Test
  public void zipUnzipTest() throws URISyntaxException, IOException {
    final File tmpfolder = File.createTempFile("smaller-zip-test", ".dir");
    tmpfolder.delete();
    tmpfolder.mkdir();
    try {
      final File zipfile = File.createTempFile("smaller-zip-test", ".zip");
      try {
        final FileOutputStream fos = new FileOutputStream(zipfile);
        final File infile = new File(getClass().getResource("/zip-test")
            .toURI());
        Zip.zip(fos, infile);
        Zip.unzip(zipfile, tmpfolder);
        assertThat(new File(tmpfolder, "a.txt").exists(), is(true));
        assertThat(new File(tmpfolder, "b.js").exists(), is(true));
      } finally {
        zipfile.delete();
      }
    } finally {
      FileUtils.deleteDirectory(tmpfolder);
    }
  }

}
