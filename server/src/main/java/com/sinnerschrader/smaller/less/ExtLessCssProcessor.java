package com.sinnerschrader.smaller.less;

import java.io.File;

import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.extensions.processor.support.less.LessCss;

import com.sinnerschrader.smaller.common.Manifest;

/**
 * @author marwol
 */
public class ExtLessCssProcessor extends LessCssProcessor {

  private String base;

  /**
   * @param manifest
   * @param base
   */
  public ExtLessCssProcessor(Manifest manifest, String base) {
    super();
    this.base = base;
    for (String path : manifest.getCurrent().getIn()) {
      if (path.endsWith(".less")) {
        this.base = new File(this.base, path).getParentFile().getAbsolutePath();
      }
    }
  }

  /**
   * @see ro.isdc.wro.extensions.processor.css.LessCssProcessor#newLessCss()
   */
  @Override
  protected LessCss newLessCss() {
    return new ExtLessCss(base);
  }

}
