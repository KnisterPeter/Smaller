package de.matrixweb.smaller.osgi.utils;

/**
 * @author marwol
 */
public class Logger {

  /**
   * @param t
   *          {@link Throwable} to log
   */
  public static void log(final Throwable t) {
    t.printStackTrace();
  }

}
