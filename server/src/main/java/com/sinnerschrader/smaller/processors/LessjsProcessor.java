package com.sinnerschrader.smaller.processors;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import com.sinnerschrader.smaller.RequestContext;
import com.sinnerschrader.smaller.ProcessorChain.Type;
import com.sinnerschrader.smaller.less.ExtLessCssProcessor;

/**
 * @author markusw
 */
public class LessjsProcessor implements Processor {

  /**
   * @see com.sinnerschrader.smaller.processors.Processor#supportsType(com.sinnerschrader.smaller.ProcessorChain.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.CSS;
  }

  /**
   * @see com.sinnerschrader.smaller.processors.Processor#execute(com.sinnerschrader.smaller.RequestContext,
   *      java.lang.String)
   */
  @Override
  public String execute(final RequestContext context, final String source) throws IOException {
    final StringWriter writer = new StringWriter();
    new ExtLessCssProcessor(context.getManifest(), context.getInput().getAbsolutePath()).process(new StringReader(source), writer);
    return writer.toString();
  }

}
