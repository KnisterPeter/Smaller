package com.sinnerschrader.smaller.lib.resource;

import java.io.IOException;

import com.sinnerschrader.smaller.lib.RequestContext;
import com.sinnerschrader.smaller.lib.processors.Processor;

/**
 * @author marwol
 */
public class StringResource implements Resource {

  private final Type type;

  private final String contents;

  /**
   * @param type
   * @param contents
   */
  public StringResource(final Type type, final String contents) {
    this.type = type;
    this.contents = contents;
  }

  /**
   * @see com.sinnerschrader.smaller.lib.resource.Resource#getType()
   */
  @Override
  public Type getType() {
    return this.type;
  }

  /**
   * @see com.sinnerschrader.smaller.lib.resource.Resource#getContents()
   */
  @Override
  public String getContents() throws IOException {
    return this.contents;
  }

  /**
   * @see com.sinnerschrader.smaller.lib.resource.Resource#apply(com.sinnerschrader.smaller.lib.processors.Processor,
   *      com.sinnerschrader.smaller.lib.RequestContext)
   */
  @Override
  public Resource apply(final Processor processor, final RequestContext context) throws IOException {
    return processor.execute(context, this);
  }

}
