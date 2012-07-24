package com.sinnerschrader.smaller;

import java.io.File;
import java.io.IOException;

import org.apache.camel.Body;
import org.apache.commons.io.output.ByteArrayOutputStream;

import com.sinnerschrader.smaller.common.Zip;

/**
 * @author marwol
 */
public class ZipHandler {

  /**
   * @param context
   * @return Returns the {@link RequestContext}
   * @throws IOException
   */
  public RequestContext unzip(@Body final RequestContext context) throws IOException {
    final File base = File.createTempFile("smaller-work", ".dir");
    base.delete();
    base.mkdir();
    Zip.unzip(context.getInputZip(), base);
    context.setInput(base);
    return context;
  }

  /**
   * @param context
   * @return Returns the {@link RequestContext}
   * @throws IOException
   */
  public RequestContext zip(@Body final RequestContext context) throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Zip.zip(baos, context.getOutput());
    context.setOutputZip(baos);
    return context;
  }

}
