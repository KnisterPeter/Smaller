package com.sinnerschrader.smaller.lib.resource;

import java.awt.image.renderable.RenderContext;
import java.io.IOException;

import com.sinnerschrader.smaller.lib.RequestContext;
import com.sinnerschrader.smaller.lib.processors.Processor;

/**
 * @author marwol
 */
public interface Resource {

  /**
   * @return Returns the resource {@link Type}
   */
  Type getType();

  /**
   * @return Returns the resource content
   * @throws IOException
   */
  String getContents() throws IOException;

  /**
   * @param processor
   *          The {@link Processor} to apply to this resource
   * @param context
   *          The {@link RenderContext}
   * @return Returns the processed {@link Resource} (could be the same instance)
   * @throws IOException
   */
  Resource apply(Processor processor, RequestContext context) throws IOException;

}
