package de.matrixweb.smaller.resource;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Field;

import org.junit.Test;

import de.matrixweb.vfs.VFS;
import de.matrixweb.vfs.wrapped.JavaFile;

/**
 * @author marwol
 */
public class ResourceResolverTest {

  /**
   * @throws Exception
   */
  @Test
  public void testRelativeFileResolving() throws Exception {
    final VFS vfs = new VFS();
    try {
      final Field field = vfs.getClass().getDeclaredField("host");
      field.setAccessible(true);
      final String host = (String) field.get(vfs);

      vfs.mount(vfs.find("/"), new JavaFile(new File("/tmp")));
      final ResourceResolver resolver = new VFSResourceResolver(vfs);
      final Resource abc = resolver.resolve("/abc.txt");
      assertThat(abc.getPath(), is("/abc.txt"));
      assertThat(abc.getURL().toString(), is("vfs://" + host + "/abc.txt"));

      final Resource def = abc.getResolver().resolve("subfolder/def.txt");
      assertThat(def.getPath(), is("/subfolder/def.txt"));
      assertThat(def.getURL().toString(), is("vfs://" + host
          + "/subfolder/def.txt"));

      final Resource ghi = def.getResolver().resolve("../subfolder2/ghi.txt");
      assertThat(ghi.getPath(), is("/subfolder2/ghi.txt"));
      assertThat(ghi.getURL().toString(), is("vfs://" + host
          + "/subfolder2/ghi.txt"));

      final Resource jkl = ghi.getResolver().resolve("/jkl.txt");
      assertThat(jkl.getPath(), is("/jkl.txt"));
      assertThat(jkl.getURL().toString(), is("vfs://" + host + "/jkl.txt"));
    } finally {
      vfs.dispose();
    }
  }

}
