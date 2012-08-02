package com.sinnerschrader.smaller.lib.less;

import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.extensions.processor.support.less.LessCss;

/**
 * @author marwol
 */
public class ExtLessCssProcessor extends LessCssProcessor {

  private final String base;

  /**
   * @param manifest
   * @param base
   */
  public ExtLessCssProcessor(final String base) {
    super();
    this.base = base;
  }

  /**
   * @see ro.isdc.wro.extensions.processor.css.LessCssProcessor#newLessCss()
   */
  @Override
  protected LessCss newLessCss() {
    return new ExtLessCss(this.base);
  }

}
