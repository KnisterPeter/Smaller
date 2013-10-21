package de.matrixweb.smaller.resource.vfs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

/**
 * @author markusw
 */
public final class VFSUtils {

  private VFSUtils() {
  }

  /**
   * @param source
   * @param target
   * @throws IOException
   */
  public static void copy(final VFile source, final VFile target)
      throws IOException {
    pipe(source, target, new Pipe() {
      @Override
      public void exec(final Reader reader, final Writer writer)
          throws IOException {
        IOUtils.copy(reader, writer);
      }
    });
  }

  /**
   * @param file
   *          The {@link VFile} to create a writer for
   * @return Returns a buffering writer for the given file
   * @throws IOException
   */
  public static Writer createWriter(final VFile file) throws IOException {
    return new BufferedWriter(new OutputStreamWriter(file.getOutputStream(),
        "UTF-8"));
  }

  /**
   * @param file
   *          The {@link VFile} to create a reader for
   * @return Returns a buffering reader for the given file
   * @throws IOException
   */
  public static Reader createReader(final VFile file) throws IOException {
    return new BufferedReader(new InputStreamReader(file.getInputStream(),
        "UTF-8"));
  }

  /**
   * Writes the given {@link CharSequence} to the given {@link VFile}.
   * 
   * @param file
   * @param cs
   * @throws IOException
   */
  public static void write(final VFile file, final CharSequence cs)
      throws IOException {
    final OutputStream out = file.getOutputStream();
    try {
      IOUtils.write(cs, out, "UTF-8");
    } finally {
      out.close();
    }
  }

  /**
   * Reads the given {@link VFile} into a {@link String}.
   * 
   * @param file
   * @return Returns the file content as {@link String}
   * @throws IOException
   */
  public static String readToString(final VFile file) throws IOException {
    final InputStream in = file.getInputStream();
    try {
      return IOUtils.toString(in, "UTF-8");
    } finally {
      in.close();
    }
  }

  /**
   * @param source
   * @param target
   * @param pipe
   * @throws IOException
   */
  public static void pipe(final VFile source, final VFile target,
      final Pipe pipe) throws IOException {
    final Reader reader = VFSUtils.createReader(source);
    try {
      final Writer writer = VFSUtils.createWriter(target);
      try {
        pipe.exec(reader, writer);
      } finally {
        IOUtils.closeQuietly(writer);
      }
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  /**
   * @author marwol
   */
  public static interface Pipe {

    /**
     * @param reader
     * @param writer
     * @throws IOException
     */
    void exec(Reader reader, Writer writer) throws IOException;

  }

}
