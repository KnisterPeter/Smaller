package com.sinnerschrader.smaller.lib.processors;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import ro.isdc.wro.extensions.processor.css.YUICssCompressorProcessor;

import com.sinnerschrader.smaller.lib.ProcessorChain.Type;
import com.sinnerschrader.smaller.lib.RequestContext;
import com.sinnerschrader.smaller.lib.resource.Resource;
import com.sinnerschrader.smaller.lib.resource.StringResource;

/**
 * @author marwol
 */
public class YuicompressorProcessor implements Processor {

  @Override
  public boolean supportsType(final Type type) {
    return type == Type.CSS;
  }

  @Override
  public Resource execute(final RequestContext context, final Resource resource) throws IOException {
    final StringWriter writer = new StringWriter();
    new YUICssCompressorProcessor().process(new StringReader(resource.getContents()), writer);
    return new StringResource(resource.getType(), writer.toString());
  }

}
