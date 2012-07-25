package com.sinnerschrader.smaller.processors;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URL;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;

import com.sinnerschrader.smaller.RequestContext;
import com.sinnerschrader.smaller.ProcessorChain.Type;
import com.sinnerschrader.smaller.cssembed.CssDataUriPostProcessor;
import com.sinnerschrader.smaller.cssembed.CssDataUriPostProcessorException;

/**
 * @author marwol
 */
public class CssembedProcessor implements Processor {

  @Override
  public boolean supportsType(final Type type) {
    return type == Type.CSS;
  }

  @Override
  public String execute(final RequestContext context, final String source) throws IOException {
    final StringWriter writer = new StringWriter();

    final WroConfiguration configuration = new WroConfiguration();
    Context.set(Context.standaloneContext(), configuration);
    final CssDataUriPostProcessor processor = new CssDataUriPostProcessor(context.getManifest(), context.getInput().getAbsolutePath());
    this.setUriLocatorFactory(processor);
    processor.process(new StringReader(source), writer);

    return writer.toString();
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
