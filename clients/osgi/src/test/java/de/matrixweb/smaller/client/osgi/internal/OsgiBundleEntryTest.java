package de.matrixweb.smaller.client.osgi.internal;

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

    final OsgiBundleEntry entry = new OsgiBundleEntry(this.bundle, "/js");
    assertThat(entry.isDirectory(), is(true));
  }

  /** */
  @Test
  public void testList() {
    final Vector<String> entries = new Vector<String>();
    entries.add("js/test1.coffee");
    entries.add("js/test2.coffee");
    when(this.bundle.getEntryPaths("/js")).thenReturn(entries.elements());

    final OsgiBundleEntry entry = new OsgiBundleEntry(this.bundle, "/js");
    final List<WrappedSystem> list = entry.list();
    assertThat(list.size(), is(2));
    assertThat(list.get(0).getName(), is("test1.coffee"));
  }

  /** */
  @Test
  public void testGetName() {
    assertThat(new OsgiBundleEntry(this.bundle, "/js").getName(), is("js"));
    assertThat(new OsgiBundleEntry(this.bundle, "js/foo.bar").getName(),
        is("foo.bar"));
  }

}
