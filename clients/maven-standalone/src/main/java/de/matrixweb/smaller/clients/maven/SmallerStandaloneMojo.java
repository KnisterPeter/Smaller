package de.matrixweb.smaller.clients.maven;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import de.matrixweb.smaller.clients.common.ExecutionException;
import de.matrixweb.smaller.common.Task;
import de.matrixweb.smaller.pipeline.Pipeline;
import de.matrixweb.smaller.pipeline.Result;
import de.matrixweb.smaller.resource.FileResourceResolver;
import de.matrixweb.smaller.resource.Type;
import de.matrixweb.smaller.resource.impl.JavaEEProcessorFactory;

/**
 * @author marwol
 * @goal smaller
 * @phase process-resources
 */
public class SmallerStandaloneMojo extends SmallerMojo {

  /**
   * @see de.matrixweb.smaller.clients.maven.SmallerMojo#executeSmaller(java.io.File,
   *      java.lang.String[], java.io.File, java.lang.String, java.lang.String,
   *      java.lang.String, java.lang.String,
   *      de.matrixweb.smaller.common.Task[])
   */
  @Override
  protected void executeSmaller(final File base, final String[] includedFiles,
      final File target, final String host, final String port,
      final String proxyhost, final String proxyport, final Task[] tasks)
      throws ExecutionException {
    try {
      final Result result = new Pipeline(new JavaEEProcessorFactory()).execute(
          new FileResourceResolver(base.getAbsolutePath()), tasks[0]);
      for (final String out : tasks[0].getOut()) {
        for (final Type type : Type.values()) {
          if (type.isOfType(FilenameUtils.getExtension(out))) {
            FileUtils.writeStringToFile(new File(target, out), result.get(type)
                .getContents());
          }
        }
      }
    } catch (final IOException e) {
      throw new ExecutionException("Embedded smaller failed", e);
    }
  }

}
