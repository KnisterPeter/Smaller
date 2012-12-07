package de.matrixweb.smaller.jpegtran;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.resource.BinaryResource;
import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.Type;

/**
 * @author markusw
 */
public class JpegtranProcessor implements Processor {

  /**
   * @see de.matrixweb.smaller.resource.Processor#supportsType(de.matrixweb.smaller.resource.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.IMAGE;
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.smaller.resource.Resource,
   *      java.util.Map)
   */
  @Override
  public Resource execute(final Resource resource,
      final Map<String, String> options) throws IOException {
    if (!resource.getPath().endsWith("jpg")
        && !resource.getPath().endsWith("jpeg")) {
      return resource;
    }
    try {
      final ProcessBuilder pb = new ProcessBuilder("jpegtran", "-optimize");
      pb.redirectError();
      final Process process = pb.start();
      IOUtils.write(((BinaryResource) resource).getBytes(),
          process.getOutputStream());
      final int result = process.waitFor();
      // TODO: File checking
      return new BinaryResource(resource.getResolver(), resource.getType(),
          resource.getPath(), IOUtils.toByteArray(process.getInputStream()));
    } catch (final InterruptedException e) {
      throw new SmallerException("Execution of jpegtran interruped", e);
    }
  }

}
