package de.matrixweb.smaller.osgi.http;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.Matchers;

import de.matrixweb.smaller.common.Task;
import de.matrixweb.smaller.common.Version;
import de.matrixweb.smaller.common.Zip;
import de.matrixweb.smaller.pipeline.Pipeline;
import de.matrixweb.smaller.pipeline.Result;
import de.matrixweb.smaller.resource.VFSResourceResolver;
import de.matrixweb.vfs.VFS;

/**
 * @author marwol
 */
public class ServletTest {

  /**
   * @throws Exception
   */
  @Test
  public void testService() throws Exception {
    final Result result = mock(Result.class);
    final Pipeline pipeline = mock(Pipeline.class);
    when(
        pipeline.execute(Matchers.isA(Version.class), Matchers.isA(VFS.class),
            Matchers.isA(VFSResourceResolver.class), Matchers.isA(Task.class)))
        .thenReturn(result);

    final HttpServletRequest request = mock(HttpServletRequest.class);
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Zip.zip(baos, new File(getClass().getResource("/servlet-test").toURI()));
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(request.getRequestURI()).thenReturn("/");
    when(request.getInputStream()).thenReturn(new InStream(baos.toByteArray()));

    final HttpServletResponse response = mock(HttpServletResponse.class);
    final OutStream out = new OutStream();
    when(response.getOutputStream()).thenReturn(out);

    final Servlet servlet = new Servlet(pipeline);
    servlet.service(request, response);

    verify(response).setHeader("X-Smaller-Status", "OK");

    final byte[] zip = out.getBytes();
    assertThat(zip.length > 0, is(true));
  }

  private static class InStream extends ServletInputStream {

    private final byte[] bytes;

    private int cursor = 0;

    /**
     * @param bytes
     */
    public InStream(final byte[] bytes) {
      this.bytes = bytes;
    }

    /**
     * @see java.io.InputStream#read()
     */
    @Override
    public int read() throws IOException {
      if (this.cursor >= this.bytes.length) {
        return -1;
      }
      return this.bytes[this.cursor++];
    }

    /**
     * @see java.io.InputStream#available()
     */
    @Override
    public int available() throws IOException {
      return this.bytes.length - this.cursor;
    }

  }

  private static class OutStream extends ServletOutputStream {

    private byte[] buf;

    private int len = 0;

    /**
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write(final int b) throws IOException {
      if (this.buf == null) {
        this.buf = new byte[1024];
      }
      if (this.len >= this.buf.length) {
        final byte[] temp = new byte[this.buf.length + 1024];
        System.arraycopy(this.buf, 0, temp, 0, this.buf.length);
        this.buf = temp;
      }
      this.buf[this.len++] = (byte) b;
    }

    public byte[] getBytes() {
      final byte[] result = new byte[this.len];
      System.arraycopy(this.buf, 0, result, 0, this.len);
      return result;
    }

  }

}
