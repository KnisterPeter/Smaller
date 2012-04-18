package com.sinnerschrader.smaller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Property;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;

/**
 * @author marwol
 */
public class ZipHandler {

  /**
   * @param exchange
   * @param temp
   * @throws IOException
   */
  public void unzip(Exchange exchange, @Body File temp) throws IOException {
    File base = File.createTempFile("smaller-", ".dir");
    base.delete();
    base.mkdir();
    unzip(temp, base);
    temp.delete();
    exchange.setProperty(Router.PROP_DIRECTORY, base);
  }

  /**
   * @param zip
   *          The zip file
   * @param target
   *          The target directory
   * @throws IOException
   */
  public void unzip(File zip, File target) throws IOException {
    ZipFile zipFile = new ZipFile(zip);
    try {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (entry.isDirectory()) {
          FileUtils.forceMkdir(new File(target, entry.getName()));
        } else {
          FileUtils.forceMkdir(new File(target, entry.getName()).getParentFile());
          
          InputStream in = null;
          FileOutputStream out = null;
          try {
            in = zipFile.getInputStream(entry);
            out = new FileOutputStream(new File(target, entry.getName()));
            IOUtils.copy(in, out);
          } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
          }
        }
      }
    } finally {
      zipFile.close();
    }
  }

  /**
   * @param base
   * @return the zip file as stream
   * @throws IOException
   */
  public byte[] zip(@Property(Router.PROP_DIRECTORY) File base) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(baos);
    zipDirectory(zos, base, base);
    zos.close();
    return baos.toByteArray();
  }

  private void zipDirectory(ZipOutputStream zos, File root, File base) throws IOException {
    String[] dirList = base.list();
    for (int i = 0; i < dirList.length; i++) {
      File f = new File(base, dirList[i]);
      if (f.isDirectory()) {
        zipDirectory(zos, root, f);
      } else {
        FileInputStream fis = new FileInputStream(f);
        ZipEntry anEntry = new ZipEntry(StringUtils.removeStart(f.getPath(), root.getPath() + '/'));
        zos.putNextEntry(anEntry);
        IOUtils.copy(fis, zos);
        fis.close();
      }
    }
  }

}
