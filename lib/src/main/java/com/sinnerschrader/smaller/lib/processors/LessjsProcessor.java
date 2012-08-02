package com.sinnerschrader.smaller.lib.processors;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import com.sinnerschrader.smaller.lib.ProcessorChain.Type;
import com.sinnerschrader.smaller.lib.less.ExtLessCssProcessor;
import com.sinnerschrader.smaller.lib.resource.Resource;
import com.sinnerschrader.smaller.lib.resource.StringResource;

/**
 * @author markusw
 */
public class LessjsProcessor implements Processor {

  /**
   * @see com.sinnerschrader.smaller.lib.processors.Processor#supportsType(com.sinnerschrader.smaller.lib.ProcessorChain.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.CSS;
  }

  /**
   * @see com.sinnerschrader.smaller.lib.processors.Processor#execute(com.sinnerschrader.smaller.lib.resource.Resource)
   */
  @Override
  public Resource execute(final Resource resource) throws IOException {
    final StringWriter writer = new StringWriter();

    String base = resource.getPath();
    int idx = base.lastIndexOf('/');
    base = base.substring(0, idx + 1);

    new ExtLessCssProcessor(base).process(new StringReader(resource.getContents()), writer);
    return new StringResource(resource.getType(), resource.getPath(), writer.toString());
  }

}
