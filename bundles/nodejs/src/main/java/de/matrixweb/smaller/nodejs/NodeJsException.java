package de.matrixweb.smaller.nodejs;

import java.io.IOException;

/**
 * @author markusw
 */
public class NodeJsException extends IOException {

  private static final long serialVersionUID = -1803769150577336117L;

  protected NodeJsException() {
    super();
  }

  /**
   * @param message
   */
  public NodeJsException(final String message) {
    super(message);
  }

  /**
   * @param message
   * @param cause
   */
  public NodeJsException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
