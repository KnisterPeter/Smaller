package com.sinnerschrader.smaller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author marwol
 */
public class Zip {

  /**
   * @param zos
   *          The output stream to write to
   * @param dir
   *          The directory to zip
   * @throws IOException
   */
  public static void zip(OutputStream out, File dir) throws IOException {
    recursiveZip(out, dir, dir);
  }

  private static void recursiveZip(OutputStream out, File root, File base) throws IOException {
    ZipOutputStream zos = new ZipOutputStream(out);
    String[] dirList = base.list();
    for (int i = 0; i < dirList.length; i++) {
      File f = new File(base, dirList[i]);
System.err.println("Adding file: " + f);
      if (f.isDirectory()) {
        recursiveZip(zos, root, f);
      } else {
        FileInputStream fis = new FileInputStream(f);
        ZipEntry anEntry = new ZipEntry(StringUtils.removeStart(f.getPath(), root.getPath() + '/'));
System.err.println("entry: " + anEntry);
        zos.putNextEntry(anEntry);
        IOUtils.copy(fis, zos);
        fis.close();
      }
    }
  }

  /**
   * @param zip
   *          The zip file
   * @param target
   *          The target directory
   * @throws IOException
   */
  public static void unzip(File zip, File target) throws IOException {
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

}
