package com.sinnerschrader.smaller;

/**
 * @author marwol
 */
public class TaskException extends RuntimeException {

  private static final long serialVersionUID = -48423895461135639L;

  /**
   * @param message
   * @param cause
   */
  public TaskException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   */
  public TaskException(final String message) {
    super(message);
  }

}
