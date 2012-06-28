package com.sinnerschrader.smaller.cssembed;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.impl.css.AbstractCssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssDataUriPreProcessor;

import com.sinnerschrader.smaller.Utils;
import com.sinnerschrader.smaller.common.Manifest;

/**
 * @author marwol
 */
public class CssDataUriPostProcessor extends CssDataUriPreProcessor {

  private final String cssUri;

  private Method parseCss;

  /**
   * @param manifest
   * @param cssUri
   */
  public CssDataUriPostProcessor(Manifest manifest, String cssUri) {
    super();

    for (String path : manifest.getCurrent().getIn()) {
      if (Utils.getResourceType(path) == ResourceType.CSS) {
        cssUri = new File(cssUri, path).getAbsolutePath();
        break;
      }
    }
    this.cssUri = cssUri;

    try {
      parseCss = AbstractCssUrlRewritingProcessor.class.getDeclaredMethod("parseCss", String.class, String.class);
      parseCss.setAccessible(true);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
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
      final String result = (String) parseCss.invoke(this, css, "file:" + cssUri);
      writer.write(result);
      onProcessCompleted();
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e.getCause());
    } finally {
      reader.close();
      writer.close();
    }
  }

}
