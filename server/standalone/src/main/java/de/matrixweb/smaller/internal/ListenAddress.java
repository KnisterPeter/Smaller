package de.matrixweb.smaller.internal;

/**
 * @author marwol
 */
public class ListenAddress {

  private String addr = "127.0.0.1";

  private String port = "1148";

  /**
   * @param params
   */
  public ListenAddress(final String... params) {
    if (params.length == 1 && params[0] != null) {
      this.port = params[0];
    } else if (params.length >= 2 && params[0] != null && params[1] != null) {
      this.port = params[0];
      this.addr = params[1];
    }
  }

  /**
   * @return Returns the host (ip) to bind to
   */
  public String getHost() {
    return this.addr;
  }

  /**
   * @return Returns the port to bind to
   */
  public int getPort() {
    return Integer.parseInt(this.port);
  }

  /**
   * @return Returns the host-port combination concatenated with a colon
   */
  public String httpAddress() {
    return this.addr + ":" + this.port;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return httpAddress();
  }

}
