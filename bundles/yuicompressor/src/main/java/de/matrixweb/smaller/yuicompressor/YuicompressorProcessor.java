package de.matrixweb.smaller.yuicompressor;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yahoo.platform.yui.compressor.CssCompressor;

import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.ProcessorUtil;
import de.matrixweb.smaller.resource.ProcessorUtil.ProcessorCallback;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.Type;
import de.matrixweb.smaller.resource.vfs.VFS;

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
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.smaller.resource.vfs.VFS,
   *      de.matrixweb.smaller.resource.Resource, java.util.Map)
   */
  @Override
  public Resource execute(final VFS vfs, final Resource resource,
      final Map<String, String> options) throws IOException {
    try {
      return ProcessorUtil.process(vfs, resource, "css",
          new ProcessorCallback() {
            @Override
            public void call(final Reader reader, final Writer writer)
                throws IOException {
              final CssCompressor compressor = new CssCompressor(
                  new StringReader(resource.getContents()));
              final int linebreakpos = -1;
              compressor.compress(writer, linebreakpos);
            }
          });
    } catch (final StackOverflowError e) {
      LOGGER.error(
          "Failed to run yuicompressor on source:\n" + resource.getContents(),
          e);
      return resource;
    }
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#dispose()
   */
  @Override
  public void dispose() {
  }

}
