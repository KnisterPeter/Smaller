package de.matrixweb.smaller.cssembed;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import net.nczonline.web.cssembed.CSSURLEmbedder;
import net.nczonline.web.cssembed.Embedder;
import net.nczonline.web.datauri.DataURIGenerator;
import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.StringResource;
import de.matrixweb.smaller.resource.Type;

/**
 * @author marwol
 */
public class CssembedProcessor implements Processor {

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
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.smaller.resource.Resource,
   *      java.util.Map)
   */
  @Override
  public Resource execute(final Resource resource,
      final Map<String, String> options) throws IOException {
    final StringWriter writer = new StringWriter();

    String root = resource.getPath();
    final int idx = root.lastIndexOf('/');
    root = root.substring(0, idx + 1);

    final int processorOptions = CSSURLEmbedder.DATAURI_OPTION
        | CSSURLEmbedder.SKIP_MISSING_OPTION;
    int maxUriLength = CSSURLEmbedder.DEFAULT_MAX_URI_LENGTH;
    if (options.containsKey("max-uri-length")) {
      maxUriLength = Integer.parseInt(options.get("max-uri-length"));
    }
    int maxImageSize = 0;
    if (options.containsKey("max-image-size")) {
      maxImageSize = Integer.parseInt(options.get("max-image-size"));
    }
    new Embedder(resource, new StringReader(resource.getContents()),
        processorOptions, true, maxUriLength, maxImageSize).embedImages(writer,
        root);

    return new StringResource(resource.getResolver(), resource.getType(),
        resource.getPath(), writer.toString());
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
