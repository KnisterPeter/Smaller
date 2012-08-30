package de.matrixweb.smaller.osgi.telnetd;

/**
 * @author marwol
 */
public class CommandException extends RuntimeException {

  private static final long serialVersionUID = -6692957917486747409L;

  /**
   * @param message
   * @param cause
   */
  public CommandException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * @param cause
   */
  public CommandException(final Throwable cause) {
    super(cause);
  }

}
