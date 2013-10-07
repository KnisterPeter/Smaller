package de.matrixweb.smaller.resource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author marwol
 */
public class ResourceIO {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ResourceIO.class);

  private final File target;

  /**
   * @throws IOException
   */
  public ResourceIO() throws IOException {
    this.target = File.createTempFile("smaller-resource", ".dir");
    this.target.delete();
    this.target.mkdirs();
  }

  /**
   * @return the target
   */
  public String getTarget() {
    return this.target.getAbsolutePath();
  }

  /**
   * @param resource
   * @throws IOException
   */
  public void write(final Resource resource) throws IOException {
    if (resource instanceof MultiResource) {
      for (final Resource r : ((MultiResource) resource).getResources()) {
        write(r);
      }
    } else {
      FileUtils.write(new File(this.target, resource.getRelativePath()),
          resource.getContents());
    }
  }

  /**
   * @return Returns the result resource
   */
  public Resource read() {
    final ResourceResolver resolver = new FileResourceResolver(
        this.target.getAbsolutePath());
    final List<Resource> resources = read(resolver, this.target);
    if (resources.size() > 1) {
      return new MultiResource(resolver, this.target.getAbsolutePath(),
          resources);
    }
    return resources.get(0);
  }

  private List<Resource> read(final ResourceResolver resolver,
      final File current) {
    final List<Resource> resources = new ArrayList<Resource>();
    for (final File file : current.listFiles()) {
      if (file.isDirectory()) {
        resources.addAll(read(resolver, file));
      } else {
        resources.add(resolver.resolve(file.getAbsolutePath().substring(
            this.target.getAbsolutePath().length() + 1)));
      }
    }
    return resources;
  }

  /**
   * 
   */
  public void dispose() {
    try {
      FileUtils.deleteDirectory(this.target);
    } catch (final IOException e) {
      LOGGER.warn("Failed to cleanup smaller resource folder", e);
    }
  }

}
