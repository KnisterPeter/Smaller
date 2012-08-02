package com.sinnerschrader.smaller.lib.resource;

import java.io.IOException;

/**
 * @author marwol
 */
public class StringResource implements Resource {

  private Type type;

  private String contents;

  /**
   * @param type
   * @param contents
   */
  public StringResource(Type type, String contents) {
    this.type = type;
    this.contents = contents;
  }

  /**
   * @see com.sinnerschrader.smaller.lib.resource.Resource#getType()
   */
  @Override
  public Type getType() {
    return type;
  }

  /**
   * @see com.sinnerschrader.smaller.lib.resource.Resource#getContents()
   */
  @Override
  public String getContents() throws IOException {
    return contents;
  }

}
