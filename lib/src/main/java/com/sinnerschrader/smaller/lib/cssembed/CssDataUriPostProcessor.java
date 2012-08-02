package com.sinnerschrader.smaller.lib.cssembed;

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

import com.sinnerschrader.smaller.common.Manifest;
import com.sinnerschrader.smaller.lib.Utils;

/**
 * @author marwol
 */
public class CssDataUriPostProcessor extends CssDataUriPreProcessor {

  private String cssUri;

  private Method parseCss;

  /**
   * @param manifest
   * @param cssUri
   */
  public CssDataUriPostProcessor(final Manifest manifest, final String cssUri) {
    super();

    this.cssUri = cssUri;
    for (final String path : manifest.getCurrent().getIn()) {
      if (Utils.getResourceType(path) == ResourceType.CSS) {
        this.cssUri = new File(cssUri, path).getAbsolutePath();
        break;
      }
    }

    try {
      parseCss = AbstractCssUrlRewritingProcessor.class.getDeclaredMethod("parseCss", String.class, String.class);
      parseCss.setAccessible(true);
    } catch (final NoSuchMethodException e) {
      throw new CssDataUriPostProcessorException("Failed to call 'parseCss'", e);
    }
  }

  /**
   * @see ro.isdc.wro.model.resource.processor.impl.css.AbstractCssUrlRewritingProcessor#process(java.io.Reader,
   *      java.io.Writer)
   */
  @Override
  public void process(final Reader reader, final Writer writer) throws IOException {
    try {
      final String css = IOUtils.toString(reader);
      final String result = (String) parseCss.invoke(this, css, "file:" + cssUri);
      writer.write(result);
      this.onProcessCompleted();
    } catch (final IllegalAccessException e) {
      throw new CssDataUriPostProcessorException("Failed to invoke 'parseCss'", e);
    } catch (final InvocationTargetException e) {
      throw new CssDataUriPostProcessorException("Failed to invoke 'parseCss'", e);
    } finally {
      reader.close();
      writer.close();
    }
  }

}
