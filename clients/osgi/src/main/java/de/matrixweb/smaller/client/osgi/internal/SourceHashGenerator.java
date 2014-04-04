package de.matrixweb.smaller.client.osgi.internal;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import de.matrixweb.smaller.client.osgi.HashGenerator;
import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.vfs.VFS;

public class SourceHashGenerator implements HashGenerator {

  public String createVersionHash(VFS vfs) {
    try {
      File temp = File.createTempFile("smaller-hash-generator", ".dir");
      try {
        temp.delete();
        temp.mkdirs();

        vfs.exportFS(temp);
        String source = createAllSources(temp);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(source.getBytes("UTF-8"));
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
      } finally {
        FileUtils.deleteDirectory(temp);
      }
    } catch (IOException e) {
      throw new SmallerException("Failed to create version-hash", e);
    }
  }

  String createAllSources(File dir) throws IOException {
    List<File> files = new ArrayList<File>(FileUtils.listFiles(dir, null, true));
    Collections.sort(files);
    StringBuilder builder = new StringBuilder();
    for (File file : files) {
      builder.append(FileUtils.readFileToString(file));
    }
    return builder.toString();
  }

}
