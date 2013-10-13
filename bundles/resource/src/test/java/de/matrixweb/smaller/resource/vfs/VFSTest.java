package de.matrixweb.smaller.resource.vfs;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.matrixweb.smaller.resource.vfs.wrapped.JavaFile;

/**
 * @author markusw
 */
public class VFSTest {

  private VFS vfs;

  /**
   * 
   */
  @Before
  public void setUp() {
    this.vfs = new VFS();
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
  public void testRoot() throws IOException {
    final VFile root = this.vfs.find("/");
    assertThat(root.getName(), is("/"));
    assertThat(root.getPath(), is("/"));
    assertThat(root.getParent(), is(root));
  }

  /**
   * @throws IOException
   */
  @Test
  public void testUrls() throws IOException {
    String url = this.vfs.find("/").getURL().toString();
    assertThat(url.startsWith("vfs://"), is(true));
    assertThat(url.endsWith("/"), is(true));

    final VFile file = this.vfs.find("/some/more/comple.path");
    url = file.getURL().toString();
    assertThat(url.startsWith("vfs://"), is(true));
    assertThat(url.endsWith("/some/more/comple.path"), is(true));
  }

  /**
   * @throws IOException
   */
  @Test
  public void testFindNonExistingFile() throws IOException {
    final VFile root = this.vfs.find("/");
    final VFile file = this.vfs.find("/some.file");
    assertThat(file.getName(), is("some.file"));
    assertThat(file.getPath(), is("/some.file"));
    assertThat(file.exists(), is(false));
    assertThat(file.getParent(), is(root));
  }

  /**
   * @throws IOException
   */
  @Test
  public void testFileReferences() throws IOException {
    final VFile test1 = this.vfs.find("/folder/test.file");
    final VFile test2 = this.vfs.find("/folder/test.file");
    assertThat(test1, is(sameInstance(test2)));
    final VFile test3 = this.vfs.find("/folder2/../folder/test.file");
    assertThat(test1, is(sameInstance(test3)));
  }

  /**
   * @throws IOException
   */
  @Test
  public void testDirectoryStructures() throws IOException {
    final VFile dir1 = this.vfs.find("/dir1");
    final VFile sub1 = this.vfs.find("/dir1/sub1");
    assertThat(dir1, is(sameInstance(sub1.getParent())));
    assertThat(dir1.find("sub1"), is(sameInstance(sub1)));
    assertThat(sub1.find("/dir1"), is(dir1));
  }

  /**
   * @throws IOException
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   */
  @Test
  public void testWriteToNewFile() throws IOException, NoSuchFieldException,
      IllegalAccessException {
    final byte[] data = "Hello world!".getBytes();

    final VFile file = this.vfs.find("/some/new/file");
    final BufferedOutputStream out = new BufferedOutputStream(
        file.getOutputStream());
    out.write(data);
    out.close();

    final Field field = file.getClass().getDeclaredField("content");
    field.setAccessible(true);
    final byte[] bytes = (byte[]) field.get(file);
    assertThat(bytes, is(data));
  }

  /**
   * @throws IOException
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   */
  @Test
  public void testReadFromFile() throws IOException, NoSuchFieldException,
      IllegalAccessException {
    final String data = "Hello world!";

    VFile file = this.vfs.find("/some/new/file");
    final BufferedOutputStream out = new BufferedOutputStream(
        file.getOutputStream());
    out.write(data.getBytes());
    out.close();

    file = this.vfs.find("/some/new/file");
    final BufferedReader reader = new BufferedReader(new InputStreamReader(
        file.getInputStream()));
    assertThat(reader.readLine(), is("Hello world!"));
    reader.close();
  }

  /**
   * @throws IOException
   */
  @Test
  public void testMounting() throws IOException {
    File dir = null;
    try {
      dir = File.createTempFile("smaller-vfs-test", ".dir");
      dir.delete();
      dir.mkdirs();
      FileUtils.write(new File(dir, "file1"), "file1");
      final File sub = new File(dir, "sub");
      sub.mkdir();
      FileUtils.write(new File(sub, "file2"), "file2");

      final VFile vdir = this.vfs.mount(this.vfs.find("/dir"),
          new JavaFile(dir));
      assertThat(vdir.isDirectory(), is(true));
      assertThat(vdir, is(sameInstance(this.vfs.find("/dir"))));
      final VFile vsub = vdir.find("sub");
      assertThat(vsub, is(sameInstance(this.vfs.find("/dir/sub"))));
      final VFile vfile1 = vdir.find("file1");
      assertThat(VFSUtils.readToString(vfile1), is("file1"));
      final VFile vfile2 = vdir.find("sub/file2");
      assertThat(VFSUtils.readToString(vfile2), is("file2"));
    } finally {
      if (dir != null) {
        FileUtils.deleteDirectory(dir);
      }
    }
  }

  /**
   * @throws IOException
   */
  @Test
  public void testMountAndWrite() throws IOException {
    File dir = null;
    try {
      dir = File.createTempFile("smaller-vfs-test", ".dir");
      dir.delete();
      dir.mkdirs();
      FileUtils.write(new File(dir, "file1"), "file1");

      this.vfs.mount(this.vfs.find("/dir"), new JavaFile(dir));

      final VFile file = this.vfs.find("/dir/file1");
      VFSUtils.write(file, "Hello world!");

      assertThat(VFSUtils.readToString(file), is("Hello world!"));
      assertThat(FileUtils.readFileToString(new File(dir, "file1")),
          is("file1"));
    } finally {
      if (dir != null) {
        FileUtils.deleteDirectory(dir);
      }
    }
  }

  /**
   * @throws IOException
   */
  @Test
  public void testExportFS() throws IOException {
    File target = null;
    try {
      target = File.createTempFile("smaller-vfs-test", ".dir");
      target.delete();
      target.mkdirs();

      VFSUtils.write(this.vfs.find("/dir1/a.txt"), "a.txt");
      VFSUtils.write(this.vfs.find("/dir1/b.txt"), "b.txt");
      VFSUtils.write(this.vfs.find("/dir1/sub/c.txt"), "c.txt");
      VFSUtils.write(this.vfs.find("/dir2/d.txt"), "d.txt");
      VFSUtils.write(this.vfs.find("/dir2/e.txt"), "e.txt");

      this.vfs.exportFS(target);

      assertThat(FileUtils.readFileToString(new File(target, "dir1/a.txt")),
          is("a.txt"));
      assertThat(FileUtils.readFileToString(new File(target, "dir1/b.txt")),
          is("b.txt"));
      assertThat(
          FileUtils.readFileToString(new File(target, "dir1/sub/c.txt")),
          is("c.txt"));
      assertThat(FileUtils.readFileToString(new File(target, "dir2/d.txt")),
          is("d.txt"));
      assertThat(FileUtils.readFileToString(new File(target, "dir2/e.txt")),
          is("e.txt"));
    } finally {
      if (target != null) {
        FileUtils.deleteDirectory(target);
      }
    }
  }

  /**
   * @throws IOException
   */
  @Test
  public void testImportFS() throws IOException {
    File source = null;
    try {
      source = File.createTempFile("smaller-vfs-test", ".dir");
      source.delete();
      source.mkdirs();
      FileUtils.write(new File(source, "dir1/a.txt"), "a.txt");
      FileUtils.write(new File(source, "dir1/b.txt"), "b.txt");
      FileUtils.write(new File(source, "dir1/sub/c.txt"), "c.txt");
      FileUtils.write(new File(source, "dir2/d.txt"), "d.txt");
      FileUtils.write(new File(source, "dir2/e.txt"), "e.txt");

      this.vfs.importFS(source);

      assertThat(VFSUtils.readToString(this.vfs.find("/dir1/a.txt")),
          is("a.txt"));
      assertThat(VFSUtils.readToString(this.vfs.find("/dir1/b.txt")),
          is("b.txt"));
      assertThat(VFSUtils.readToString(this.vfs.find("/dir1/sub/c.txt")),
          is("c.txt"));
      assertThat(VFSUtils.readToString(this.vfs.find("/dir2/d.txt")),
          is("d.txt"));
      assertThat(VFSUtils.readToString(this.vfs.find("/dir2/e.txt")),
          is("e.txt"));
    } finally {
      if (source != null) {
        FileUtils.deleteDirectory(source);
      }
    }
  }

  /**
   * @throws IOException
   */
  @Test
  public void testStack() throws IOException {
    VFile file = this.vfs.find("/some/path/to/a/file.txt");
    VFSUtils.write(file, "file.txt 1.0");

    this.vfs.stack();

    file = this.vfs.find("/some/path/to/a/file.txt");
    assertThat(VFSUtils.readToString(file), is("file.txt 1.0"));
    VFSUtils.write(file, "file.txt 2.0");
    assertThat(VFSUtils.readToString(file), is("file.txt 2.0"));
  }

}
