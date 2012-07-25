package com.sinnerschrader.smaller.processors;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import ro.isdc.wro.extensions.processor.css.YUICssCompressorProcessor;

import com.sinnerschrader.smaller.RequestContext;
import com.sinnerschrader.smaller.ProcessorChain.Type;

/**
 * @author marwol
 */
public class YuicompressorProcessor implements Processor {

  @Override
  public boolean supportsType(final Type type) {
    return type == Type.CSS;
  }

  @Override
  public String execute(final RequestContext context, final String source) throws IOException {
    final StringWriter writer = new StringWriter();
    new YUICssCompressorProcessor().process(new StringReader(source), writer);
    return writer.toString();
  }

}
