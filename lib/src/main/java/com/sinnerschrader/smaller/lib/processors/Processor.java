package com.sinnerschrader.smaller.lib.processors;

import java.io.IOException;

import com.sinnerschrader.smaller.lib.ProcessorChain;
import com.sinnerschrader.smaller.lib.RequestContext;

/**
 * @author marwol
 */
public interface Processor {

  /**
   * @param type
   * @return True if the given type can be handled by this processor
   */
  boolean supportsType(ProcessorChain.Type type);

  /**
   * @param context
   * @param source
   * @return Returns the transformed source
   * @throws IOException
   */
  String execute(RequestContext context, String source) throws IOException;

}
