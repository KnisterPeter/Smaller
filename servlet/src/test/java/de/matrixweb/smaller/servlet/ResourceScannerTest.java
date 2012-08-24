package de.matrixweb.smaller.servlet;

import java.util.Set;

import org.junit.Test;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;

import de.matrixweb.smaller.servlet.ResourceScanner;

import static org.junit.Assert.*;

import static org.hamcrest.Matchers.*;

/**
 * @author marwol
 */
public class ResourceScannerTest {

  /**
   * 
   */
  @Test
  public void testGetResources() {
    MockServletContext servletContext = new MockServletContext("src/test/resources", new FileSystemResourceLoader());
    String[] includes = new String[] { "**/css/**", "private/a.css", "external/*/a.css" };
    String[] excludes = new String[] { "**/css/b.css" };
    Set<String> resources = new ResourceScanner(servletContext, includes, excludes).getResources();
    assertThat(resources, hasItems("/css/a.css", "/private/css/a.css", "/private/a.css", "/external/v2/a.css"));
    assertThat(resources, not(hasItems("/external/a.css")));
    assertThat(resources, not(hasItems("/css/b.css")));
  }

}
