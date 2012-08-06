package com.sinnerschrader.smaller.internal;

/**
 * @author marwol
 */
public class ListenAddress {

  private String addr = "127.0.0.1";

  private String port = "1148";

  public ListenAddress(final String... params) {
    if (params.length == 1 && params[0] != null) {
      port = params[0];
    } else if (params.length >= 2 && params[0] != null && params[1] != null) {
      port = params[0];
      addr = params[1];
    }
  }

  public String getHost() {
    return addr;
  }

  public int getPort() {
    return Integer.parseInt(port);
  }

  public String httpAddress() {
    return addr + ":" + port;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.httpAddress();
  }

}
