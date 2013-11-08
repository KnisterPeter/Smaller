package de.matrixweb.smaller.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;

/**
 * @author markusw
 */
public final class Main {

  private Main() {
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    try {
      ClassLoader cl = Main.class.getClassLoader();
      final String classpath = System.getProperty("java.class.path");
      if (!classpath.contains(":")) {
        cl = prepareClassPath(classpath);
      }
      Class.forName("de.matrixweb.smaller.internal.Server", true, cl)
          .getMethod("main", String[].class).invoke(null, (Object) args);
    } catch (final IOException e) {
      throw new FatalServerException("Fatal Server Error", e);
    } catch (final IllegalAccessException e) {
      throw new FatalServerException("Fatal Server Error", e);
    } catch (final InvocationTargetException e) {
      throw new FatalServerException("Fatal Server Error", e);
    } catch (final NoSuchMethodException e) {
      throw new FatalServerException("Fatal Server Error", e);
    } catch (final ClassNotFoundException e) {
      throw new FatalServerException("Fatal Server Error", e);
    }
  }

  private static ClassLoader prepareClassPath(final String classpath)
      throws IOException {
    final JarFile jar = new JarFile(classpath);
    try {
      final List<URL> urls = copyJarEntries(jar, createTempStorage());
      urls.add(new URL("file:" + classpath));
      return new URLClassLoader(urls.toArray(new URL[urls.size()]), null);
    } finally {
      jar.close();
    }
  }

  private static List<URL> copyJarEntries(final JarFile jar, final File temp)
      throws IOException {
    final List<URL> urls = new LinkedList<URL>();
    final Enumeration<JarEntry> e = jar.entries();
    while (e.hasMoreElements()) {
      final JarEntry entry = e.nextElement();
      if (entry.getName().endsWith(".jar")) {
        final File file = copyFile(jar, entry, temp);
        urls.add(new URL("file:" + file.getAbsolutePath()));
      }
    }
    return urls;
  }

  private static File copyFile(final JarFile jar, final JarEntry entry,
      final File temp) throws IOException {
    final File file = new File(temp, entry.getName());
    InputStream is = null;
    try {
      is = jar.getInputStream(entry);
      FileOutputStream os = null;
      try {
        file.getParentFile().mkdirs();
        os = new FileOutputStream(file);
        final byte[] buf = new byte[1024];
        int len = is.read(buf);
        while (len > -1) {
          os.write(buf, 0, len);
          len = is.read(buf);
        }
      } finally {
        IOUtils.closeQuietly(os);
      }
    } finally {
      IOUtils.closeQuietly(is);
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
        for (final File file : temp.listFiles()) {
          file.delete();
        }
        temp.delete();
      }
    });
    return temp;
  }

  private static class FatalServerException extends RuntimeException {

    private static final long serialVersionUID = 4541747502527240103L;

    /**
     * @param message
     * @param cause
     */
    public FatalServerException(final String message, final Throwable cause) {
      super(message, cause);
    }

  }

}
