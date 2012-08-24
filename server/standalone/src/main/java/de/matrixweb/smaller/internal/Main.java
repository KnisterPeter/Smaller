package de.matrixweb.smaller.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author markusw
 */
public class Main {

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {
    ClassLoader cl = Main.class.getClassLoader();
    String classpath = System.getProperty("java.class.path");
    if (!classpath.contains(":")) {
      cl = prepareClassPath(classpath);
    }
    Class.forName("de.matrixweb.smaller.internal.Server", true, cl)
        .getMethod("main", String[].class).invoke(null, (Object) args);
  }

  private static ClassLoader prepareClassPath(String classpath)
      throws IOException, MalformedURLException, FileNotFoundException {
    JarFile jar = new JarFile(classpath);
    try {
      List<URL> urls = copyJarEntries(jar, createTempStorage());
      urls.add(new URL("file:" + classpath));
      return new URLClassLoader(urls.toArray(new URL[urls.size()]), null);
    } finally {
      jar.close();
    }
  }

  private static List<URL> copyJarEntries(JarFile jar, final File temp)
      throws IOException, FileNotFoundException, MalformedURLException {
    List<URL> urls = new LinkedList<URL>();
    Enumeration<JarEntry> e = jar.entries();
    while (e.hasMoreElements()) {
      JarEntry entry = e.nextElement();
      if (entry.getName().endsWith(".jar")) {
        File file = copyFile(jar, entry, temp);
        urls.add(new URL("file:" + file.getAbsolutePath()));
      }
    }
    return urls;
  }

  private static File copyFile(JarFile jar, JarEntry entry, final File temp)
      throws IOException, FileNotFoundException {
    File file = new File(temp, entry.getName());
    InputStream is = jar.getInputStream(entry);
    FileOutputStream os = null;
    try {
      os = new FileOutputStream(file);
      byte[] buf = new byte[1024];
      int len = is.read(buf);
      while (len > -1) {
        os.write(buf, 0, len);
        len = is.read(buf);
      }
    } finally {
      is.close();
      os.close();
    }
    return file;
  }

  private static File createTempStorage() throws IOException {
    final File temp = File.createTempFile("smaller-classpath-", ".dir");
    temp.delete();
    temp.mkdirs();
    temp.deleteOnExit();
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        for (File file : temp.listFiles()) {
          file.delete();
        }
        temp.delete();
      }
    });
    return temp;
  }

}
