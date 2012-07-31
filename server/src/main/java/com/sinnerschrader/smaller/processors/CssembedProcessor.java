package com.sinnerschrader.smaller.processors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import net.nczonline.web.cssembed.CSSURLEmbedder;
import net.nczonline.web.datauri.DataURIGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;

import com.sinnerschrader.smaller.ProcessorChain.Type;
import com.sinnerschrader.smaller.RequestContext;
import com.sinnerschrader.smaller.Utils;
import com.sinnerschrader.smaller.common.SmallerException;
import com.sinnerschrader.smaller.cssembed.CssDataUriPostProcessor;
import com.sinnerschrader.smaller.cssembed.CssDataUriPostProcessorException;

/**
 * @author marwol
 */
public class CssembedProcessor implements Processor {

  /**
   * 
   */
  public CssembedProcessor() {
    this.patchCssEmbedd();
  }

  @Override
  public boolean supportsType(final Type type) {
    return type == Type.CSS;
  }

  @Override
  public String execute(final RequestContext context, final String source) throws IOException {
    final StringWriter writer = new StringWriter();

    final PrintStream err = System.err;
    try {
      System.setErr(new PrintStream(new LoggerOutputStream(), true));

      String root = context.getInput().getAbsolutePath() + File.separatorChar;
      for (final String path : context.getManifest().getCurrent().getIn()) {
        if (Utils.getResourceType(path) == ResourceType.CSS) {
          root = new File(root, path).getParentFile().getAbsolutePath() + File.separatorChar;
          break;
        }
      }

      final int options = CSSURLEmbedder.DATAURI_OPTION;
      final int maxUriLength = 0;
      final int maxImageSize = 0;
      final CSSURLEmbedder embedder = new CSSURLEmbedder(new StringReader(source), options, true, maxUriLength, maxImageSize);
      embedder.embedImages(writer, root);
    } finally {
      System.setErr(err);
    }

    return writer.toString();
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void patchCssEmbedd() {
    try {
      final Field imageTypes = CSSURLEmbedder.class.getDeclaredField("imageTypes");
      imageTypes.setAccessible(true);
      ((Set) imageTypes.get(null)).add("svg");

      final Field binaryTypes = DataURIGenerator.class.getDeclaredField("binaryTypes");
      binaryTypes.setAccessible(true);
      ((Map) binaryTypes.get(null)).put("svg", "image/svg+xml");
    } catch (final NoSuchFieldException e) {
      throw new SmallerException("Failed to patch cssembed", e);
    } catch (final IllegalAccessException e) {
      throw new SmallerException("Failed to patch cssembed", e);
    }
  }

  private void setUriLocatorFactory(final CssDataUriPostProcessor processor) {
    try {
      final Field uriLocatorFactory = processor.getClass().getSuperclass().getDeclaredField("uriLocatorFactory");
      uriLocatorFactory.setAccessible(true);
      uriLocatorFactory.set(processor, new SimpleUriLocatorFactory());
    } catch (final NoSuchFieldException e) {
      throw new CssDataUriPostProcessorException("No field named uriLocatorFactory found", e);
    } catch (final IllegalAccessException e) {
      throw new CssDataUriPostProcessorException("Not allowed to access uriLocatorFactory", e);
    }
  }

  private static class LoggerOutputStream extends OutputStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerOutputStream.class);

    private final StringBuilder buf = new StringBuilder();

    /**
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write(final int b) throws IOException {
      buf.append((char) b);
    }

    /**
     * @see java.io.OutputStream#flush()
     */
    @Override
    public void flush() throws IOException {
      LOGGER.info(buf.toString());
      buf.setLength(0);
    }

  }

  private static class SimpleUriLocatorFactory implements UriLocatorFactory {

    /**
     * @see ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory#locate(java.lang.String)
     */
    @Override
    public InputStream locate(final String uri) throws IOException {
      return new URL(uri).openStream();
    }

    /**
     * @see ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory#getInstance(java.lang.String)
     */
    @Override
    public UriLocator getInstance(final String uri) {
      throw new UnsupportedOperationException("Not implemented 'getInstance() on " + uri + "'");
    }

  }

}
