package de.matrixweb.smaller.cssembed;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;

import net.nczonline.web.cssembed.CSSURLEmbedder;
import net.nczonline.web.cssembed.Embedder;
import net.nczonline.web.datauri.DataURIGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.ProcessorUtil;
import de.matrixweb.smaller.resource.ProcessorUtil.ProcessorCallback;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.Type;
import de.matrixweb.vfs.VFS;

/**
 * @author marwol
 */
public class CssembedProcessor implements Processor {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(CssembedProcessor.class);

  /**
   * 
   */
  public CssembedProcessor() {
    patchCssEmbedd();
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#supportsType(de.matrixweb.smaller.resource.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.CSS;
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.vfs.VFS,
   *      de.matrixweb.smaller.resource.Resource, java.util.Map)
   */
  @Override
  public Resource execute(final VFS vfs, final Resource resource,
      final Map<String, Object> options) throws IOException {
    try {
      return ProcessorUtil.process(vfs, resource, "css",
          new ProcessorCallback() {
            @Override
            public void call(final Reader reader, final Writer writer)
                throws IOException {
              String root = resource.getPath();
              final int idx = root.lastIndexOf('/');
              root = root.substring(0, idx + 1);

              final int processorOptions = CSSURLEmbedder.DATAURI_OPTION
                  | CSSURLEmbedder.SKIP_MISSING_OPTION;
              final int maxUriLength = getMaxUriLength(options);
              final int maxImageSize = getMaxImageSize(options);

              new Embedder(resource, new StringReader(resource.getContents()),
                  processorOptions, true, maxUriLength, maxImageSize)
                  .embedImages(writer, root);
            }
          });
    } catch (final UnknownHostException e) {
      // TODO: Rework to skip only missing resources
      LOGGER.warn("Missing resource - skipping cssembed", e);
      return resource;
    }
  }

  private int getMaxUriLength(final Map<String, Object> options) {
    final Object value = options.get("max-uri-length");
    return value == null ? CSSURLEmbedder.DEFAULT_MAX_URI_LENGTH : Integer
        .parseInt(value.toString());
  }

  private int getMaxImageSize(final Map<String, Object> options) {
    final Object value = options.get("max-image-size");
    return value == null ? 0 : Integer.parseInt(value.toString());
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#dispose()
   */
  @Override
  public void dispose() {
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void patchCssEmbedd() {
    try {
      final Field imageTypes = CSSURLEmbedder.class
          .getDeclaredField("imageTypes");
      imageTypes.setAccessible(true);
      ((Set) imageTypes.get(null)).add("svg");

      final Field binaryTypes = DataURIGenerator.class
          .getDeclaredField("binaryTypes");
      binaryTypes.setAccessible(true);
      ((Map) binaryTypes.get(null)).put("svg", "image/svg+xml");
    } catch (final NoSuchFieldException e) {
      throw new SmallerException("Failed to patch cssembed", e);
    } catch (final IllegalAccessException e) {
      throw new SmallerException("Failed to patch cssembed", e);
    }
  }

}
