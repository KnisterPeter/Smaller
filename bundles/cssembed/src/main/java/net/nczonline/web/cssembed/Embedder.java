package net.nczonline.web.cssembed;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import net.nczonline.web.datauri.DataURIGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.resource.Resource;

/**
 * @author marwol
 */
public class Embedder extends CSSURLEmbedder {

  private static final Logger LOGGER = LoggerFactory.getLogger(Embedder.class);

  private static final Method GET_MIME_TYPE;
  static {
    try {
      GET_MIME_TYPE = DataURIGenerator.class.getDeclaredMethod("getMimeType",
          String.class, String.class);
      GET_MIME_TYPE.setAccessible(true);
    } catch (final NoSuchMethodException e) {
      throw new SmallerException("getMimeType is not found", e);
    }
  }

  private final Resource resource;

  /**
   * @param resource
   * @param in
   * @param options
   * @param verbose
   * @param maxUriLength
   * @param maxImageSize
   * @throws IOException
   */
  public Embedder(final Resource resource, final Reader in, final int options,
      final boolean verbose, final int maxUriLength, final int maxImageSize)
      throws IOException {
    super(in, options, verbose, maxUriLength, maxImageSize);
    this.resource = resource;
  }

  /**
   * @see net.nczonline.web.cssembed.CSSURLEmbedder#getImageURIString(java.lang.String,
   *      java.lang.String)
   */
  @Override
  String getImageURIString(final String url, final String originalUrl)
      throws IOException {
    if (isImage(url)) {
      try {
        URL imgurl = null;
        if (url.startsWith("http://")) {
          imgurl = new URL(url);
        } else {
          Resource img = this.resource.getResolver().resolve(url);
          imgurl = img.getURL();
          if (imgurl == null) {
            // Note: This fixes absolute urls which are relative to the servlet
            // root (e.g. '/some/where.png')
            // CssEmbed always prepends none absolute urls (not starting with
            // http:) with the given root string
            img = this.resource.getResolver().resolve(originalUrl);
            imgurl = img.getURL();
          }
        }
        if (imgurl != null) {
          final StringWriter writer = new StringWriter();
          DataURIGenerator.generate(imgurl, writer,
              (String) GET_MIME_TYPE.invoke(null, originalUrl, (String) null));
          return writer.toString();
        } else {
          LOGGER.info("Skipping on {} - does not resolve to resource url",
              originalUrl);
        }
      } catch (final IOException e) {
        LOGGER.info("Not resolvable resource {} - try default embedding",
            originalUrl);
      } catch (final IllegalAccessException e) {
        LOGGER.info("Failed to fetch mime-type of {} - try default embedding",
            originalUrl, e);
      } catch (final InvocationTargetException e) {
        LOGGER.info("Failed to fetch mime-type of {} - try default embedding",
            originalUrl, e.getTargetException());
      }
    }
    return super.getImageURIString(url, originalUrl);
  }

}
