package de.matrixweb.smaller.client.osgi.internal;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

public class SourceHashGeneratorTest {

  @Test
  public void testSourceConcatenation() throws IOException {
    File dir1 = null;
    File dir2 = null;
    try {
      dir1 = File.createTempFile("test1", ".dir");
      dir1.delete();
      dir1.mkdirs();
      setupTestDir(dir1);
      dir2 = File.createTempFile("test2", ".dir");
      dir2.delete();
      dir2.mkdirs();
      setupTestDir(dir2);

      SourceHashGenerator shg = new SourceHashGenerator();
      String str1 = shg.createAllSources(dir1);
      String str2 = shg.createAllSources(dir2);

      assertThat(str1, is(str2));
    } finally {
      if (dir2 != null) {
        FileUtils.deleteDirectory(dir2);
      }
      if (dir1 != null) {
        FileUtils.deleteDirectory(dir1);
      }
    }
  }
  
  private void setupTestDir(File dir) throws IOException {
    FileUtils.write(new File(dir, "test1.txt"), "test1");
    FileUtils.write(new File(dir, "test2.txt"), "test2");
    FileUtils.write(new File(dir, "test3.txt"), "test3");
    FileUtils.write(new File(dir, "test4.txt"), "test4");
  }

}
