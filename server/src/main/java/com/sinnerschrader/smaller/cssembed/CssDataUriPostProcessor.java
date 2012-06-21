package com.sinnerschrader.smaller.cssembed;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.UnhandledException;

import ro.isdc.wro.model.resource.processor.impl.css.AbstractCssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssDataUriPreProcessor;

/**
 * @author marwol
 */
public class CssDataUriPostProcessor extends CssDataUriPreProcessor {

  private final String cssUri;

  private Method parseCss;

  /**
   * @param cssUri
   */
  public CssDataUriPostProcessor(String cssUri) {
    super();
    this.cssUri = "file:" + cssUri + "/dummy.css";
    try {
      parseCss = AbstractCssUrlRewritingProcessor.class.getDeclaredMethod("parseCss", String.class, String.class);
      parseCss.setAccessible(true);
    } catch (NoSuchMethodException e) {
      throw new UnhandledException(e);
    }
  }

  /**
   * @see ro.isdc.wro.model.resource.processor.impl.css.AbstractCssUrlRewritingProcessor#process(java.io.Reader,
   *      java.io.Writer)
   */
  @Override
  public void process(Reader reader, Writer writer) throws IOException {
    try {
      final String css = IOUtils.toString(reader);
      final String result = (String) parseCss.invoke(this, css, cssUri);
      writer.write(result);
      onProcessCompleted();
    } catch (IllegalAccessException e) {
      throw new UnhandledException(e);
    } catch (InvocationTargetException e) {
      throw new UnhandledException(e.getCause());
    } finally {
      reader.close();
      writer.close();
    }
  }

}
