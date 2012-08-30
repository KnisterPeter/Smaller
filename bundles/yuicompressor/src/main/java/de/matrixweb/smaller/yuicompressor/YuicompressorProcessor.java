package de.matrixweb.smaller.yuicompressor;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yahoo.platform.yui.compressor.CssCompressor;

import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.StringResource;
import de.matrixweb.smaller.resource.Type;

/**
 * @author marwol
 */
public class YuicompressorProcessor implements Processor {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(YuicompressorProcessor.class);

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
    final CssCompressor compressor = new CssCompressor(new StringReader(
        resource.getContents()));
    final int linebreakpos = -1;
    try {
      compressor.compress(writer, linebreakpos);
      return new StringResource(resource.getResolver(), resource.getType(),
          resource.getPath(), writer.toString());
    } catch (final StackOverflowError e) {
      LOGGER.error(
          "Failed to run yuicompressor on source:\n" + resource.getContents(),
          e);
      return resource;
    }
  }

}
