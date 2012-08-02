package com.sinnerschrader.smaller.lib.less;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.extensions.processor.support.less.LessCss;

import com.sinnerschrader.smaller.common.SmallerException;

/**
 * @author marwol
 */
public class ExtLessCss extends LessCss {

  private final String base;

  /**
   * @param base
   */
  public ExtLessCss(final String base) {
    super();
    this.base = base;
  }

  /**
   * @see ro.isdc.wro.extensions.processor.support.less.LessCss#getScriptAsStream()
   */
  @Override
  protected InputStream getScriptAsStream() {
    return ExtLessCss.class.getResourceAsStream("less-1.3.0.js");
  }

  /**
   * @see ro.isdc.wro.extensions.processor.support.less.LessCss#getRunScriptAsStream()
   */
  @Override
  protected InputStream getRunScriptAsStream() {
    try {
      final String code = IOUtils.toString(ExtLessCss.class.getResourceAsStream("run.js"));
      return new ByteArrayInputStream(("var base = 'file:" + base + "/';\n" + code).getBytes());
    } catch (final IOException e) {
      throw new SmallerException("Failed to load extended run.js", e);
    }
  }

}
