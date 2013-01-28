package de.matrixweb.smaller.clients.simple;

import org.apache.commons.io.IOUtils;

import de.matrixweb.smaller.clients.common.Logger;
import de.matrixweb.smaller.clients.common.Util;

/**
 * @author marwol
 */
public class Main {

  /**
   * @param args
   * @throws Exception
   */
  public static void main(final String[] args) throws Exception {
    String host = "sr.s2.de";
    String port = "80";
    for (int i = 0, n = args.length; i < n; i++) {
      if ("--host".equals(args[i])) {
        host = args[++i];
      } else if ("--port".equals(args[i])) {
        port = args[++i];
      } else {
        System.out.println("Usage: simple-client [--host host] [--port port]");
        System.out
            .println("\tThe input is read from stdin the output is written to stdout");
        System.exit(1);
      }
    }

    final Util util = new Util(new Logger() {
      public void debug(final String message) {
        System.err.println(message);
      }
    });
    // IOUtils.write(IOUtils.toByteArray(System.in), System.out);
    // System.exit(1);
    System.err.println(String.format("Connect to %s:%s", host, port));
    IOUtils.write(util.send(host, port, IOUtils.toByteArray(System.in)),
        System.out);
  }

}
