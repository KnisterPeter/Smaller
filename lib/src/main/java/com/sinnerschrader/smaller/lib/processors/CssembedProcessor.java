package com.sinnerschrader.smaller.lib.processors;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import net.nczonline.web.cssembed.CSSURLEmbedder;
import net.nczonline.web.datauri.DataURIGenerator;

import com.sinnerschrader.smaller.common.SmallerException;
import com.sinnerschrader.smaller.lib.ProcessorChain.Type;
import com.sinnerschrader.smaller.lib.resource.Resource;
import com.sinnerschrader.smaller.lib.resource.StringResource;

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
   * @see com.sinnerschrader.smaller.lib.processors.Processor#supportsType(com.sinnerschrader.smaller.lib.ProcessorChain.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.CSS;
  }

  /**
   * @see com.sinnerschrader.smaller.lib.processors.Processor#execute(com.sinnerschrader.smaller.lib.resource.Resource)
   */
  @Override
  public Resource execute(final Resource resource) throws IOException {
    final StringWriter writer = new StringWriter();

    String root = resource.getPath();
    int idx = root.lastIndexOf('/');
    root = root.substring(0, idx + 1);

    final int options = CSSURLEmbedder.DATAURI_OPTION;
    final int maxUriLength = 0;
    final int maxImageSize = 0;
    new CSSURLEmbedder(new StringReader(resource.getContents()), options, true,
        maxUriLength, maxImageSize).embedImages(writer, root);

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
