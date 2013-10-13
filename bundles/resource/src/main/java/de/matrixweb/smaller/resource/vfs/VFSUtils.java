package de.matrixweb.smaller.resource.vfs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

/**
 * @author markusw
 */
public final class VFSUtils {

  private VFSUtils() {
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

}
