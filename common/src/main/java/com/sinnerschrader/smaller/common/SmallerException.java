package com.sinnerschrader.smaller.common;

/**
 * @author marwol
 */
public class SmallerException extends RuntimeException {

  private static final long serialVersionUID = -48423895461135639L;

  /**
   * @param message
   * @param cause
   */
  public SmallerException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   */
  public SmallerException(final String message) {
    super(message);
  }

}
