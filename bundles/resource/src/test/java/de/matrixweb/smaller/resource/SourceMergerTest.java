package de.matrixweb.smaller.resource;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.matrixweb.smaller.resource.vfs.VFS;
import de.matrixweb.smaller.resource.vfs.VFSResourceResolver;
import de.matrixweb.smaller.resource.vfs.wrapped.JavaFile;

/**
 * @author rongae
 */
public class SourceMergerTest {

  private static String absoluteResourcesPath;

  private VFS vfs;

  private VFSResourceResolver resolver;

  /**
   * @throws IOException
   */
  @BeforeClass
  public static void setupTestClass() throws IOException {
    final String currentPath = new File(".").getCanonicalPath();
    absoluteResourcesPath = currentPath + "/src/test/resources";
  }

  /**
   * @throws IOException
   */
  @Before
  public void setUp() throws IOException {
    this.vfs = new VFS();
    this.vfs.mount(this.vfs.find("/"), new JavaFile(new File(
        absoluteResourcesPath)));

    this.resolver = new VFSResourceResolver(this.vfs);
  }

  /**
   * 
   */
  @After
  public void tearDown() {
    this.vfs.dispose();
  }

  /**
   * @throws IOException
   */
  @Test
  public void testUniqueFileResolving() throws IOException {
    final SourceMerger merger = new SourceMerger(true);
    final List<String> resourcesFiles = new ArrayList<String>();
    resourcesFiles.add("basic.json");
    final List<Resource> resources = merger.getResources(this.resolver,
        resourcesFiles);
    assertThat(resources.size(), is(2));
    assertThat(resources.get(0).getURL().getPath(),
        is("/extensions/js/ext/json2.js"));
    assertThat(resources.get(1).getURL().getPath(),
        is("/extensions/js/ext/modernizr.custom.js"));
  }

  /**
   * @throws IOException
   */
  @Test
  public void testMutlipleFileResolving() throws IOException {
    final SourceMerger merger = new SourceMerger();
    final List<String> resourcesFiles = new ArrayList<String>();
    resourcesFiles.add("basic.json");
    final List<Resource> resources = merger.getResources(this.resolver,
        resourcesFiles);

    assertThat(resources.size(), is(9));

    assertThat(resources.get(0).getURL().getPath(),
        is("/extensions/js/ext/json2.js"));
    assertThat(resources.get(1).getURL().getPath(),
        is("/extensions/js/ext/modernizr.custom.js"));
    assertThat(resources.get(2).getURL().getPath(),
        is("/extensions/js/ext/modernizr.custom.js"));
    assertThat(resources.get(3).getURL().getPath(),
        is("/extensions/js/ext/json2.js"));
    assertThat(resources.get(4).getURL().getPath(),
        is("/extensions/js/ext/modernizr.custom.js"));
    assertThat(resources.get(5).getURL().getPath(),
        is("/extensions/js/ext/json2.js"));
    assertThat(resources.get(6).getURL().getPath(),
        is("/extensions/js/ext/json2.js"));
    assertThat(resources.get(7).getURL().getPath(),
        is("/extensions/js/ext/modernizr.custom.js"));
    assertThat(resources.get(8).getURL().getPath(),
        is("/extensions/js/ext/json2.js"));
  }
}
