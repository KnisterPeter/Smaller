package com.sinnerschrader.smaller.lib.processors;

import java.io.IOException;

import com.sinnerschrader.smaller.lib.ProcessorChain;
import com.sinnerschrader.smaller.lib.resource.Resource;

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
   * @param resource
   * @return Returns the transformed source
   * @throws IOException
   */
  Resource execute(Resource resource) throws IOException;

}
