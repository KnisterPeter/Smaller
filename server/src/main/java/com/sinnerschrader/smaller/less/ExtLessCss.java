package com.sinnerschrader.smaller.less;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.extensions.processor.support.less.LessCss;

/**
 * @author marwol
 */
public class ExtLessCss extends LessCss {

  private String base;

  /**
   * @param base
   */
  public ExtLessCss(String base) {
    super();
    this.base = base;
  }

  /**
   * @see ro.isdc.wro.extensions.processor.support.less.LessCss#getRunScriptAsStream()
   */
  @Override
  protected InputStream getRunScriptAsStream() {
    try {
      String code = IOUtils.toString(ExtLessCss.class.getResourceAsStream("run.js"));
      return new ByteArrayInputStream(("var base = 'file:" + base + "/';\n" + code).getBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @see ro.isdc.wro.extensions.processor.support.less.LessCss#getScriptAsStream()
   */
  @Override
  protected InputStream getScriptAsStream() {
    return ExtLessCss.class.getResourceAsStream("less-1.3.0.js");
  }

}
