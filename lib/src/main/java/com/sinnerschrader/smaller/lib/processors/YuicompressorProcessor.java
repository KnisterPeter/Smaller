package com.sinnerschrader.smaller.lib.processors;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import com.sinnerschrader.smaller.resource.Processor;
import com.sinnerschrader.smaller.resource.Resource;
import com.sinnerschrader.smaller.resource.StringResource;
import com.sinnerschrader.smaller.resource.Type;
import com.yahoo.platform.yui.compressor.CssCompressor;

/**
 * @author marwol
 */
public class YuicompressorProcessor implements Processor {

  /**
   * @see com.sinnerschrader.smaller.resource.Processor#supportsType(com.sinnerschrader.smaller.resource.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.CSS;
  }

  /**
   * @see com.sinnerschrader.smaller.resource.Processor#canMerge()
   */
  @Override
  public boolean canMerge() {
    return false;
  }

  /**
   * @see com.sinnerschrader.smaller.resource.Processor#execute(com.sinnerschrader.smaller.resource.Resource)
   */
  @Override
  public Resource execute(final Resource resource) throws IOException {
    final StringWriter writer = new StringWriter();
    final CssCompressor compressor = new CssCompressor(new StringReader(
        resource.getContents()));
    final int linebreakpos = -1;
    compressor.compress(writer, linebreakpos);
    return new StringResource(resource.getResolver(), resource.getType(),
        resource.getPath(), writer.toString());
  }

}
