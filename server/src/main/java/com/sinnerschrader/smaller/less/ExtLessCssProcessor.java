package com.sinnerschrader.smaller.less;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.sinnerschrader.smaller.common.Manifest;

import ro.isdc.wro.extensions.processor.css.LessCssProcessor;
import ro.isdc.wro.extensions.processor.support.less.LessCss;
import ro.isdc.wro.model.resource.Resource;

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
    for (String path: manifest.getCurrent().getIn()) {
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

  /**
   * @see ro.isdc.wro.extensions.processor.css.LessCssProcessor#process(ro.isdc.wro.model.resource.Resource,
   *      java.io.Reader, java.io.Writer)
   */
  @Override
  public void process(Resource resource, Reader reader, Writer writer) throws IOException {
    super.process(resource, reader, writer);
  }

}
