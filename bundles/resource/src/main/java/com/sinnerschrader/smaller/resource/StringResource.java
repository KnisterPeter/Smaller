package com.sinnerschrader.smaller.resource;

import java.io.IOException;

/**
 * @author marwol
 */
public class StringResource implements Resource {

  private final ResourceResolver resolver;
  
  private final Type type;

  private final String path;

  private final String contents;

  /**
   * @param type
   * @param path
   * @param contents
   */
  public StringResource(final ResourceResolver resolver, final Type type, final String path, final String contents) {
    this.resolver = resolver;
    this.type = type;
    this.path = path;
    this.contents = contents;
  }

  /**
   * @see com.sinnerschrader.smaller.resource.Resource#getResolver()
   */
  @Override
  public ResourceResolver getResolver() {
    return this.resolver;
  }
  
  /**
   * @see com.sinnerschrader.smaller.resource.Resource#getType()
   */
  @Override
  public Type getType() {
    return this.type;
  }

  /**
   * @see com.sinnerschrader.smaller.resource.Resource#getPath()
   */
  @Override
  public String getPath() {
    return this.path;
  }

  /**
   * @see com.sinnerschrader.smaller.resource.Resource#getContents()
   */
  @Override
  public String getContents() throws IOException {
    return this.contents;
  }

  /**
   * @see com.sinnerschrader.smaller.resource.Resource#apply(com.sinnerschrader.smaller.lib.processors.Processor)
   */
  @Override
  public Resource apply(final Processor processor) throws IOException {
    return processor.execute(this);
  }

}
