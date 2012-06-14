package com.sinnerschrader.smaller.less;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.extensions.processor.support.less.LessCss;
import ro.isdc.wro.model.resource.Resource;

/**
 * @author marwol
 */
public class ExtLessCssProcessor extends LessCssProcessor {

  private String base;

  /**
   * @param base
   */
  public ExtLessCssProcessor(String base) {
    super();
    this.base = base;
  }

  /**
   * @see ro.isdc.wro.extensions.processor.css.LessCssProcessor#newLessCss()
   */
  @Override
  protected LessCss newLessCss() {
    return new ExtLessCss(base);
  }

  /**
   * @see ro.isdc.wro.extensions.processor.css.LessCssProcessor#process(ro.isdc.wro.model.resource.Resource,
   *      java.io.Reader, java.io.Writer)
   */
  @Override
  public void process(Resource resource, Reader reader, Writer writer) throws IOException {
    super.process(resource, reader, writer);
  }

}
