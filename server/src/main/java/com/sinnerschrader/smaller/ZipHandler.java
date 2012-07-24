package com.sinnerschrader.smaller;

import java.io.File;
import java.io.IOException;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Property;
import org.apache.commons.io.output.ByteArrayOutputStream;

import com.sinnerschrader.smaller.common.Zip;

/**
 * @author marwol
 */
public class ZipHandler {

  /**
   * @param exchange
   * @param temp
   * @throws IOException
   */
  public void unzip(final Exchange exchange, @Body final File temp) throws IOException {
    final File base = File.createTempFile("smaller-work", ".dir");
    base.delete();
    base.mkdir();
    Zip.unzip(temp, base);
    temp.delete();
    exchange.setProperty(Router.PROP_INPUT, base);
  }

  /**
   * @param output
   * @return the zip file as stream
   * @throws IOException
   */
  public byte[] zip(@Property(Router.PROP_OUTPUT) final File output) throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Zip.zip(baos, output);
    return baos.toByteArray();
  }

}
