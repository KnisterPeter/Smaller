package de.matrixweb.smaller.osgi.maven;

import java.io.File;
import java.io.IOException;

/**
 * @author markusw
 */
public interface MavenInstaller {

  /**
   * @param mvnURN
   * @throws IOException
   */
  void installOrUpdate(String mvnURN) throws IOException;

  /**
   * @param update
   * @param file
   * @throws IOException
   */
  void installOrUpdate(boolean update, File... file) throws IOException;

}
