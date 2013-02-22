package de.matrixweb.smaller.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author marwol
 */
public final class Zip {

  private Zip() {
  }

  /**
   * @param out
   *          The output stream to write to
   * @param dir
   *          The directory to zip
   * @throws IOException
   */
  public static void zip(final OutputStream out, final File dir)
      throws IOException {
    final ZipOutputStream zos = new ZipOutputStream(out);
    recursiveZip(zos, dir, dir);
    zos.close();
  }

  private static void recursiveZip(final ZipOutputStream zos, final File root,
      final File base) throws IOException {
    final String[] dirList = base.list();
    for (final String element : dirList) {
      final File f = new File(base, element);
      if (f.isDirectory()) {
        recursiveZip(zos, root, f);
      } else {
        final FileInputStream fis = new FileInputStream(f);
        try {
          if (!FilenameUtils.isExtension(f.getName(),
              Arrays.asList("js", "coffee", "ts", "json", "css", "less"))) {
            writeDeflate(f, fis, zos, root);
          } else {
            writeStored(f, fis, zos, root);
          }
        } finally {
          IOUtils.closeQuietly(fis);
        }
      }
    }
  }

  private static void writeDeflate(final File f, final FileInputStream fis,
      final ZipOutputStream zos, final File root) throws IOException {
    final ZipEntry anEntry = new ZipEntry(
        FilenameUtils.separatorsToUnix(StringUtils.removeStart(f.getPath(),
            root.getPath() + File.separator)));
    zos.putNextEntry(anEntry);
    IOUtils.copy(fis, zos);
    zos.closeEntry();
  }

  private static void writeStored(final File f, final FileInputStream fis,
      final ZipOutputStream zos, final File root) throws IOException {
    final byte[] bytes = IOUtils.toByteArray(fis);

    final CRC32 crc = new CRC32();
    crc.reset();
    crc.update(bytes);

    final ZipEntry anEntry = new ZipEntry(
        FilenameUtils.separatorsToUnix(StringUtils.removeStart(f.getPath(),
            root.getPath() + File.separator)));
    anEntry.setMethod(ZipEntry.STORED);
    anEntry.setCompressedSize(f.length());
    anEntry.setSize(f.length());
    anEntry.setCrc(crc.getValue());
    zos.putNextEntry(anEntry);
    IOUtils.write(bytes, zos);
    zos.closeEntry();
  }

  /**
   * @param zip
   *          The zip file
   * @param target
   *          The target directory
   * @throws IOException
   */
  public static void unzip(final File zip, final File target)
      throws IOException {
    final ZipFile zipFile = new ZipFile(zip);
    try {
      final Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        final ZipEntry entry = entries.nextElement();
        if (entry.isDirectory()) {
          FileUtils.forceMkdir(new File(target, entry.getName()));
        } else {
          FileUtils.forceMkdir(new File(target, entry.getName())
              .getParentFile());

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
