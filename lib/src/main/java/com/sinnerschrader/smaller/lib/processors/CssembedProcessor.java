package com.sinnerschrader.smaller.lib.processors;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import net.nczonline.web.cssembed.CSSURLEmbedder;
import net.nczonline.web.datauri.DataURIGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.ResourceType;

import com.sinnerschrader.smaller.common.SmallerException;
import com.sinnerschrader.smaller.lib.ProcessorChain.Type;
import com.sinnerschrader.smaller.lib.resource.Resource;
import com.sinnerschrader.smaller.lib.resource.StringResource;
import com.sinnerschrader.smaller.lib.RequestContext;
import com.sinnerschrader.smaller.lib.Utils;

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
  public Resource execute(final RequestContext context, final Resource resource) throws IOException {
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
      final CSSURLEmbedder embedder = new CSSURLEmbedder(new StringReader(resource.getContents()), options, true, maxUriLength, maxImageSize);
      embedder.embedImages(writer, root);
    } finally {
      System.setErr(err);
    }

    return new StringResource(resource.getType(), writer.toString());
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

}
