package de.matrixweb.smaller.servlet;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;

/**
 * @author marwol
 */
public class ResourceScannerTest {

  /**
   * 
   */
  @Test
  public void testGetResources() {
    final MockServletContext servletContext = new MockServletContext(
        "src/test/resources", new FileSystemResourceLoader());
    String[] includes = new String[] { "**/css/**", "private/a.css",
        "external/*/a.css" };
    final String[] excludes = new String[] { "**/css/b.css" };
    Set<String> resources = new ResourceScanner(servletContext, includes,
        excludes).getResources();
    assertThat(
        resources,
        hasItems("/css/a.css", "/private/css/a.css", "/private/a.css",
            "/external/v2/a.css"));
    assertThat(resources, not(hasItems("/external/a.css")));
    assertThat(resources, not(hasItems("/css/b.css")));

    includes = new String[] { "js/*.js" };
    resources = new ResourceScanner(servletContext, includes, excludes)
        .getResources();
    assertThat(resources, hasItems("/js/test.js"));
    assertThat(resources, not(hasItems("/existing/a.js")));
  }

}
