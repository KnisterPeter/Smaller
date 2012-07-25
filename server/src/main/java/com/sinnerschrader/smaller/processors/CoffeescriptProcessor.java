package com.sinnerschrader.smaller.processors;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import ro.isdc.wro.extensions.processor.js.CoffeeScriptProcessor;

import com.sinnerschrader.smaller.RequestContext;
import com.sinnerschrader.smaller.ProcessorChain.Type;

/**
 * @author marwol
 */
public class CoffeescriptProcessor implements Processor {

  /**
   * @see com.sinnerschrader.smaller.processors.Processor#supportsType(com.sinnerschrader.smaller.ProcessorChain.Type)
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
    new CoffeeScriptProcessor().process(new StringReader(source), writer);
    return writer.toString();
  }

}
