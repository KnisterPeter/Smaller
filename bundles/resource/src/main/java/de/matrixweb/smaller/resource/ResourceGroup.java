package de.matrixweb.smaller.resource;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.matrixweb.vfs.VFS;

/**
 * Just a wrapper to keep the {@link Resource} interface for multiple resources.
 * The only valid methods are {@link #getResources()}, {@link #getMerger()} and
 * {@link #apply(VFS, Processor, Map)}.
 * 
 * @author markusw
 */
public class ResourceGroup implements Resource {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ResourceGroup.class);

  private final List<Resource> resources;

  private final SourceMerger merger;

  /**
   * @param resources
   * @param merger
   */
  public ResourceGroup(final List<Resource> resources, final SourceMerger merger) {
    this.resources = new ArrayList<Resource>(resources);
    this.merger = merger;
  }

  /**
   * @return the resources
   */
  public List<Resource> getResources() {
    return this.resources;
  }

  /**
   * @return the merger
   */
  public SourceMerger getMerger() {
    return this.merger;
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#getResolver()
   */
  @Override
  public ResourceResolver getResolver() {
    return this.resources.get(0).getResolver();
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#getType()
   */
  @Override
  public Type getType() {
    throw new UnsupportedOperationException();
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#getPath()
   */
  @Override
  public String getPath() {
    return this.resources.get(0).getPath();
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#getURL()
   */
  @Override
  public URL getURL() throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#getContents()
   */
  @Override
  public String getContents() throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#apply(de.matrixweb.vfs.VFS,
   *      de.matrixweb.smaller.resource.Processor, java.util.Map)
   */
  @Override
  public Resource apply(final VFS vfs, final Processor processor,
      final Map<String, Object> options) throws IOException {
    // Version 1.1.0 handling
    if (this.resources.isEmpty()) {
      LOGGER.info("Found empty input-ResourcesGroup; Processor '" + processor
          + "' will decide what to process");
      return processor.execute(vfs, null, options);
    }
    if (processor instanceof MultiResourceProcessor) {
      LOGGER.info("MultiResourceProcessor '" + processor + "' execution on "
          + this);
      final Resource result = processor.execute(vfs, this, options);
      this.resources.clear();
      this.resources.add(result);
      return result;
    }
    final List<Resource> list = new ArrayList<Resource>();
    for (final Resource resource : this.resources) {
      LOGGER.info("Processor '" + processor + "' execution on " + resource);
      list.add(resource.apply(vfs, processor, options));
    }
    this.resources.clear();
    this.resources.addAll(list);
    return this;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.resources.toString();
  }

}
