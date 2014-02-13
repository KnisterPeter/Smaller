package de.matrixweb.smaller.client.osgi.internal;

import java.net.MalformedURLException;
import java.net.URL;
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

  /**
   * @throws MalformedURLException
   */
  @Test
  public void testIsDirectory() throws MalformedURLException {
    final Vector<URL> entries = new Vector<URL>();
    entries.add(new URL("file://host/js/test1.coffee"));
    entries.add(new URL("file://host/js/test2.coffee"));
    when(this.bundle.findEntries("/js", null, true)).thenReturn(entries.elements());

    final OsgiBundleEntry entry = new OsgiBundleEntry(this.bundle, "/js", null, null);
    assertThat(entry.isDirectory(), is(true));
  }

  /**
   * @throws MalformedURLException
   */
  @Test
  public void testList() throws MalformedURLException {
    final Vector<URL> entries = new Vector<URL>();
    entries.add(new URL("file://host/js/test1.coffee"));
    entries.add(new URL("file://host/js/test2.coffee"));
    entries.add(new URL("file://host/js/folder/test3.coffee"));
    when(this.bundle.findEntries("/js", null, true)).thenReturn(entries.elements());

    final OsgiBundleEntry entry = new OsgiBundleEntry(this.bundle, "/js", null, null);
    final List<WrappedSystem> list = entry.list();
    assertThat(list.size(), is(3));
    final List<String> names = new ArrayList<String>();
    for (final WrappedSystem ws : list) {
      names.add(ws.getName());
    }
    assertThat(names, hasItems("test1.coffee", "test2.coffee", "folder"));
  }

  /**
   * @throws MalformedURLException
   */
  @Test
  public void testListRoot() throws MalformedURLException {
    final Vector<URL> entries = new Vector<URL>();
    entries.add(new URL("file://host/js/test1.coffee"));
    entries.add(new URL("file://host/js/test2.coffee"));
    entries.add(new URL("file://host/js/folder/test3.coffee"));
    when(this.bundle.findEntries("/", null, true)).thenReturn(entries.elements());

    final OsgiBundleEntry entry = new OsgiBundleEntry(this.bundle, "/", null, null);
    final List<WrappedSystem> list = entry.list();
    assertThat(list.size(), is(1));
    final List<String> names = new ArrayList<String>();
    for (final WrappedSystem ws : list) {
      names.add(ws.getName());
    }
    assertThat(names, hasItems("js"));
  }

  /** */
  @Test
  public void testGetName() {
    assertThat(new OsgiBundleEntry(this.bundle, "/js", null, null).getName(), is("js"));
    assertThat(new OsgiBundleEntry(this.bundle, "js/foo.bar", null, null).getName(), is("foo.bar"));
  }

  /**
   * @throws MalformedURLException
   */
  @Test
  public void testIncludesExcludes() throws MalformedURLException {
    final Vector<URL> entries = new Vector<URL>();
    entries.add(new URL("file://host/js/test1.js"));
    entries.add(new URL("file://host/js/test2.coffee"));
    entries.add(new URL("file://host/bin/test2.js"));
    when(this.bundle.findEntries("/js", null, true)).thenReturn(entries.elements());

    final OsgiBundleEntry entry = new OsgiBundleEntry(this.bundle, "/js", new String[] { "**.js" },
        new String[] { "bin/**" });
    final List<WrappedSystem> list = entry.list();
    assertThat(list.size(), is(1));
    final List<String> names = new ArrayList<String>();
    for (final WrappedSystem ws : list) {
      names.add(ws.getName());
    }
    assertThat(names, hasItem("test1.js"));
    assertThat(names, not(hasItems("test2.js", "test2.coffee")));
  }

}
