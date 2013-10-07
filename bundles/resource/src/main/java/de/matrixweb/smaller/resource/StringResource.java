package de.matrixweb.smaller.resource;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * @author marwol
 */
public class StringResource implements Resource {

  private final ResourceResolver resolver;

  private final Type type;

  private final String path;

  private final String contents;

  /**
   * @param resolver
   * @param type
   * @param path
   * @param contents
   */
  public StringResource(final ResourceResolver resolver, final Type type,
      final String path, final String contents) {
    this.resolver = resolver;
    this.type = type;
    this.path = path;
    this.contents = contents;
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#getResolver()
   */
  @Override
  public ResourceResolver getResolver() {
    return this.resolver;
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#getType()
   */
  @Override
  public Type getType() {
    return this.type;
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#getPath()
   */
  @Override
  public String getPath() {
    return this.path;
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#getRelativePath()
   */
  @Override
  public String getRelativePath() {
    return this.path;
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#getURL()
   */
  @Override
  public URL getURL() throws IOException {
    return null;
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#getContents()
   */
  @Override
  public String getContents() throws IOException {
    return this.contents;
  }

  /**
   * @see de.matrixweb.smaller.resource.Resource#apply(de.matrixweb.smaller.resource.Processor,
   *      java.util.Map)
   */
  @Override
  public Resource apply(final Processor processor,
      final Map<String, String> options) throws IOException {
    return processor.execute(this, options);
  }

}
