package de.matrixweb.smaller.resource.vfs;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.matrixweb.smaller.resource.vfs.wrapped.JavaFile;
import de.matrixweb.smaller.resource.vfs.wrapped.MergingVFS;

/**
 * @author marwol
 */
public class VFSMergeTest {

  private VFS vfs;

  private File dir1;

  private File dir2;

  /**
   * @throws IOException
   */
  @Before
  public void setUp() throws IOException {
    this.vfs = new VFS();

    this.dir1 = File.createTempFile("smaller-vfs-merge", ".dir");
    this.dir2 = File.createTempFile("smaller-vfs-merge", ".dir");
    this.dir1.delete();
    this.dir1.mkdirs();
    this.dir2.delete();
    this.dir2.mkdirs();
    FileUtils.write(new File(this.dir1, "file1.txt"), "file1");
    FileUtils.write(new File(this.dir2, "file2.txt"), "file2");
    new File(this.dir1, "folder").mkdir();
    new File(this.dir1, "folder/sub").mkdir();
    new File(this.dir2, "folder").mkdir();

    this.vfs.mount(this.vfs.find("/"), new MergingVFS(new JavaFile(this.dir1),
        new JavaFile(this.dir2)));
  }

  /**
   * @throws IOException
   */
  @After
  public void tearDown() throws IOException {
    FileUtils.deleteDirectory(this.dir1);
    FileUtils.deleteDirectory(this.dir2);

    this.vfs.dispose();
  }

  /**
   * @throws IOException
   */
  @Test
  public void testDirectory() throws IOException {
    assertThat(this.vfs.find("/folder").isDirectory(), is(true));
    assertThat(this.vfs.find("/folder/sub").isDirectory(), is(true));
    assertThat(this.vfs.find("/folder/sub2").isDirectory(), is(false));
  }

  /**
   * @throws IOException
   */
  @Test
  public void testExistance() throws IOException {
    assertThat(this.vfs.find("/file1.txt").exists(), is(true));
    assertThat(this.vfs.find("/file2.txt").exists(), is(true));
  }

  /**
   * @throws IOException
   */
  @Test
  public void testName() throws IOException {
    assertThat(this.vfs.find("/file1.txt").getName(), is("file1.txt"));
    assertThat(this.vfs.find("/folder/sub").getName(), is("sub"));
  }

}
