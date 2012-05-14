package com.sinnerschrader.smaller.clients.common;

/**
 * @author marwol
 */
public class ExecutionException extends Exception {

  private static final long serialVersionUID = -6927648196172916895L;

  /**
   * @param message
   */
  public ExecutionException(String message) {
    super(message);
  }

  /**
   * @param message
   * @param cause
   */
  public ExecutionException(String message, Throwable cause) {
    super(message, cause);
  }

}
