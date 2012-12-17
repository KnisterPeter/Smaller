package de.matrixweb.smaller.resource;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author rongae
 */
public class SourceMergerTest {

  @Test
  public void testUniqueFileResolving() throws IOException {
    final String tempFolder = "/tmp";
    final ResourceResolver resolver = new FileResourceResolver(tempFolder);
    final SourceMerger merger = new SourceMerger(Boolean.TRUE);
    final List<String> resourcesFiles = new ArrayList<String>();
    resourcesFiles.add("basic.json");
    final List<Resource> resources = merger.getResources(resolver,
        resourcesFiles);
    assertThat(resources.size(), is(2));
    assertThat(resources.get(0).getURL().getPath(), is(tempFolder
        + "/extensions/js/ext/json2.js"));
    assertThat(resources.get(1).getURL().getPath(), is(tempFolder
        + "/extensions/js/ext/modernizr.custom.js"));
  }

  @Test
  public void testMutlipleFileResolving() throws IOException {
    final String tempFolder = "/tmp";
    final ResourceResolver resolver = new FileResourceResolver(tempFolder);
    final SourceMerger merger = new SourceMerger();
    final List<String> resourcesFiles = new ArrayList<String>();
    resourcesFiles.add("basic.json");
    final List<Resource> resources = merger.getResources(resolver,
        resourcesFiles);

    assertThat(resources.size(), is(9));

    assertThat(resources.get(0).getURL().getPath(), is(tempFolder
        + "/extensions/js/ext/json2.js"));
    assertThat(resources.get(1).getURL().getPath(), is(tempFolder
        + "/extensions/js/ext/modernizr.custom.js"));
    assertThat(resources.get(2).getURL().getPath(), is(tempFolder
        + "/extensions/js/ext/modernizr.custom.js"));
    assertThat(resources.get(3).getURL().getPath(), is(tempFolder
        + "/extensions/js/ext/json2.js"));
    assertThat(resources.get(4).getURL().getPath(), is(tempFolder
        + "/extensions/js/ext/modernizr.custom.js"));
    assertThat(resources.get(5).getURL().getPath(), is(tempFolder
        + "/extensions/js/ext/json2.js"));
    assertThat(resources.get(6).getURL().getPath(), is(tempFolder
        + "/extensions/js/ext/json2.js"));
    assertThat(resources.get(7).getURL().getPath(), is(tempFolder
        + "/extensions/js/ext/modernizr.custom.js"));
    assertThat(resources.get(8).getURL().getPath(), is(tempFolder
        + "/extensions/js/ext/json2.js"));
  }
}
