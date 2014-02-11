package de.matrixweb.smaller.client.osgi.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

import de.matrixweb.vfs.wrapped.WrappedSystem;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * @author markusw
 */
public class OsgiBundleEntryTest {

  private Bundle bundle;

  /**
   * 
   */
  @Before
  public void setUp() {
    this.bundle = mock(Bundle.class);
  }

  /** */
  @Test
  public void testIsDirectory() {
    final Vector<String> entries = new Vector<String>();
    entries.add("js/test1.coffee");
    entries.add("js/test2.coffee");
    when(this.bundle.getEntryPaths("/js")).thenReturn(entries.elements());

    final OsgiBundleEntry entry = new OsgiBundleEntry(this.bundle, "/js", null,
        null);
    assertThat(entry.isDirectory(), is(true));
  }

  /** */
  @Test
  public void testList() {
    final Vector<String> entries = new Vector<String>();
    entries.add("js/test1.coffee");
    entries.add("js/test2.coffee");
    when(this.bundle.getEntryPaths("/js")).thenReturn(entries.elements());

    final OsgiBundleEntry entry = new OsgiBundleEntry(this.bundle, "/js", null,
        null);
    final List<WrappedSystem> list = entry.list();
    assertThat(list.size(), is(2));
    final List<String> names = new ArrayList<String>();
    for (final WrappedSystem ws : list) {
      names.add(ws.getName());
    }
    assertThat(names, hasItem("test1.coffee"));
  }

  /** */
  @Test
  public void testGetName() {
    assertThat(new OsgiBundleEntry(this.bundle, "/js", null, null).getName(),
        is("js"));
    assertThat(
        new OsgiBundleEntry(this.bundle, "js/foo.bar", null, null).getName(),
        is("foo.bar"));
  }

  /** */
  @Test
  public void testIncludesExcludes() {
    final Vector<String> entries = new Vector<String>();
    entries.add("js/test1.js");
    entries.add("js/test2.coffee");
    entries.add("bin/test2.js");
    when(this.bundle.getEntryPaths("/js")).thenReturn(entries.elements());

    final OsgiBundleEntry entry = new OsgiBundleEntry(this.bundle, "/js",
        new String[] { "**.js" }, new String[] { "bin/**" });
    final List<WrappedSystem> list = entry.list();
    assertThat(list.size(), is(2));
    final List<String> names = new ArrayList<String>();
    for (final WrappedSystem ws : list) {
      names.add(ws.getName());
    }
    assertThat(names, hasItem("test1.js"));
    assertThat(names, not(hasItems("test2.js", "test2.coffee")));
  }

}
