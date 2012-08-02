package com.sinnerschrader.smaller.servlet;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

import static org.junit.Assert.*;

import static org.hamcrest.CoreMatchers.*;

/**
 * @author marwol
 * 
 */
public class SmallerServletTest {

  /**
   * @throws ServletException
   * @throws IOException
   */
  @Test
  @Ignore
  public void testGet() throws ServletException, IOException {
    MockServletContext context = new MockServletContext("src/test/resources", new FileSystemResourceLoader());
    MockServletConfig config = new MockServletConfig(context);
    config.addInitParameter("processors", "lessjs,yuicompressor");
    config.addInitParameter("includes", "**/*.css");
    config.addInitParameter("excludes", "css/b.css");
    SmallerServlet servlet = new SmallerServlet();
    servlet.init(config);

    MockHttpServletRequest request = new MockHttpServletRequest(context, "GET", "/test.css");
    MockHttpServletResponse response = new MockHttpServletResponse();
    servlet.service(request, response);
    String body = response.getContentAsString();
    assertThat(body, is("a{}"));
  }

}
