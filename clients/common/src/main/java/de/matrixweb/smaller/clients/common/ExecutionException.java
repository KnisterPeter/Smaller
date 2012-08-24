package de.matrixweb.smaller.clients.common;

/**
 * @author marwol
 */
public class ExecutionException extends Exception {

  private static final long serialVersionUID = -6927648196172916895L;

  /**
   * @param message
   */
  public ExecutionException(final String message) {
    super(message);
  }

  /**
   * @param message
   * @param cause
   */
  public ExecutionException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
