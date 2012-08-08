package com.sinnerschrader.smaller.osgi.maven;

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

}
