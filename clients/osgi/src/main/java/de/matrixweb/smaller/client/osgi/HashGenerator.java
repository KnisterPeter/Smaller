package de.matrixweb.smaller.client.osgi;

import de.matrixweb.vfs.VFS;

/**
 * Used to generate URL hashes to get around caching issues in the browser.
 * 
 * @author marwol
 */
public interface HashGenerator {

  public String createVersionHash(VFS vfs);
  
}
