package com.sinnerschrader.smaller;

/**
 * @author marwol
 */
class ListenAddress {

  private String addr = "127.0.0.1";

  private String port = "1148";

  public ListenAddress(final String... params) {
    if (params.length == 1) {
      port = params[0];
    } else if (params.length >= 2) {
      port = params[0];
      addr = params[1];
    }
  }

  String getHost() {
    return addr;
  }

  int getPort() {
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