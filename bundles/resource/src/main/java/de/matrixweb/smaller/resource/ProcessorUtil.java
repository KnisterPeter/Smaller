package de.matrixweb.smaller.resource;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.FilenameUtils;

import de.matrixweb.vfs.VFS;
import de.matrixweb.vfs.VFSUtils;
import de.matrixweb.vfs.VFSUtils.Pipe;
import de.matrixweb.vfs.VFile;

/**
 * @author marwol
 */
public final class ProcessorUtil {

  private ProcessorUtil() {
  }

  /**
   * @param vfs
   * @param input
   * @param sourceType
   * @param callback
   * @return Returns the processed {@link Resource}
   * @throws IOException
   */
  public static Resource process(final VFS vfs, final Resource input,
      final String sourceType, final ProcessorCallback callback)
      throws IOException {
    return process(vfs, input, sourceType, sourceType, callback);
  }

  /**
   * @param vfs
   * @param input
   * @param sourceType
   * @param resultType
   * @param callback
   * @return Returns the processed {@link Resource}
   * @throws IOException
   */
  public static Resource process(final VFS vfs, final Resource input,
      final String sourceType, final String resultType,
      final ProcessorCallback callback) throws IOException {
    final VFile snapshot = vfs.stack();
    try {
      final VFile source = vfs.find(input.getPath());
      final VFile target = vfs.find(FilenameUtils.removeExtension(input
          .getPath()) + "." + resultType);
      VFSUtils.pipe(source, target, new Pipe() {
        @Override
        public void exec(final Reader reader, final Writer writer)
            throws IOException {
          callback.call(reader, writer);
        }
      });
      return input != null ? input.getResolver().resolve(target.getPath())
          : null;
    } catch (final IOException e) {
      vfs.rollback(snapshot);
      throw e;
    }
  }

  /**
   * @param vfs
   * @param resource
   * @param sourceType
   * @param callback
   * @return Returns the processed {@link Resource}
   * @throws IOException
   */
  public static Resource processAllFilesOfType(final VFS vfs,
      final Resource resource, final String sourceType,
      final ProcessorCallback callback) throws IOException {
    return processAllFilesOfType(vfs, resource, sourceType, sourceType,
        callback);
  }

  /**
   * @param vfs
   * @param resource
   * @param sourceType
   * @param resultType
   * @param callback
   * @return Returns the processed {@link Resource}
   * @throws IOException
   */
  public static Resource processAllFilesOfType(final VFS vfs,
      final Resource resource, final String sourceType,
      final String resultType, final ProcessorCallback callback)
      throws IOException {
    final VFile snapshot = vfs.stack();
    try {
      // For all '.<sourceType>' files...
      for (final VFile file : ResourceUtil.getFilesByExtension(vfs, sourceType)) {
        final VFile target = vfs.find(FilenameUtils.removeExtension(file
            .getPath()) + "." + resultType);

        // ... call <processorCallback>
        VFSUtils.pipe(file, target, new Pipe() {
          @Override
          public void exec(final Reader reader, final Writer writer)
              throws IOException {
            callback.call(reader, writer);
          }
        });
      }
      // ... and return the input resource conversion result
      return resource != null ? resource.getResolver().resolve(
          FilenameUtils.removeExtension(resource.getPath()) + "." + resultType)
          : null;
    } catch (final IOException e) {
      vfs.rollback(snapshot);
      throw e;
    }
  }

  /** */
  public static interface ProcessorCallback {

    /**
     * @param reader
     * @param writer
     * @throws IOException
     */
    void call(Reader reader, Writer writer) throws IOException;

  }

}
