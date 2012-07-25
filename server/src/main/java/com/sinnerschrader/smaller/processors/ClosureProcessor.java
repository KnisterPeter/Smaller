package com.sinnerschrader.smaller.processors;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import com.sinnerschrader.smaller.RequestContext;
import com.sinnerschrader.smaller.closure.ClosureCompressorProcessor;
import com.sinnerschrader.smaller.processors.ProcessorChain.Type;

/**
 * @author marwol
 */
public class ClosureProcessor implements Processor {

  /**
   * @see com.sinnerschrader.smaller.processors.Processor#supportsType(com.sinnerschrader.smaller.processors.ProcessorChain.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.JS;
  }

  /**
   * @see com.sinnerschrader.smaller.processors.Processor#execute(com.sinnerschrader.smaller.RequestContext,
   *      java.lang.String)
   */
  @Override
  public String execute(final RequestContext context, final String source) throws IOException {
    final StringWriter writer = new StringWriter();
    new ClosureCompressorProcessor().process(new StringReader(source), writer);
    return writer.toString();
  }

}
