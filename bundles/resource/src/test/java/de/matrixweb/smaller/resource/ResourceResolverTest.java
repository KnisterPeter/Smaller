package de.matrixweb.smaller.resource;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

/**
 * @author marwol
 */
public class ResourceResolverTest {

  /**
   * @throws IOException
   */
  @Test
  public void testRelativeFileResolving() throws IOException {
    final ResourceResolver resolver = new FileResourceResolver("/tmp");
    final Resource abc = resolver.resolve("abc.txt");
    assertThat(abc.getPath(), is("/tmp/abc.txt"));
    assertThat(abc.getRelativePath(), is("abc.txt"));
    assertThat(abc.getURL().toString(), is("file:/tmp/abc.txt"));

    final Resource def = abc.getResolver().resolve("subfolder/def.txt");
    assertThat(def.getPath(), is("/tmp/subfolder/def.txt"));
    assertThat(def.getRelativePath(), is("subfolder/def.txt"));
    assertThat(def.getURL().toString(), is("file:/tmp/subfolder/def.txt"));

    final Resource ghi = def.getResolver().resolve("../subfolder2/ghi.txt");
    assertThat(ghi.getPath(), is("/tmp/subfolder/../subfolder2/ghi.txt"));
    assertThat(ghi.getRelativePath(), is("subfolder/../subfolder2/ghi.txt"));
    assertThat(ghi.getURL().toString(),
        is("file:/tmp/subfolder/../subfolder2/ghi.txt"));

    final Resource jkl = ghi.getResolver().resolve("/jkl.txt");
    assertThat(jkl.getPath(), is("/tmp/jkl.txt"));
    assertThat(jkl.getRelativePath(), is("jkl.txt"));
    assertThat(jkl.getURL().toString(), is("file:/tmp/jkl.txt"));
  }

}
