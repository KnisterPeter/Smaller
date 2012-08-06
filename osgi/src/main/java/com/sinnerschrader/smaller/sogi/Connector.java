package com.sinnerschrader.smaller.sogi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

/**
 * @author markusw
 */
public class Connector extends Thread {

  private String secret;

  private Framework framework;

  Connector(String secret, Framework framework) {
    super();
    this.secret = secret;
    this.framework = framework;
  }

  /**
   * @see java.lang.Thread#run()
   */
  @Override
  public void run() {
    ServerSocket server = null;
    try {
      server = new ServerSocket(1149);
      server.setReuseAddress(true);
      while (true) {
        handleClient(server.accept());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (server != null) {
        try {
          server.close();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  private void handleClient(Socket client) {
    try {
      BufferedInputStream in = null;
      BufferedOutputStream out = null;
      try {
        in = new BufferedInputStream(client.getInputStream());
        out = new BufferedOutputStream(client.getOutputStream());

        String location = readLocation(in);
        if (!location.startsWith(secret + ':')) {
          out.write("FAIL".getBytes());
          return;
        }
        location = location.substring(secret.length() + 1);

        installBundle(location, in, out);
      } finally {
        if (in != null) {
          in.close();
        }
        if (out != null) {
          out.close();
        }
        client.close();
      }
    } catch (IOException e) {
      // Client failed
      e.printStackTrace();
    }
  }

  private String readLocation(InputStream in) throws IOException {
    StringBuilder buf = new StringBuilder();
    char c = (char) in.read();
    while (c != '\n') {
      buf.append(c);
      c = (char) in.read();
    }
    return buf.toString();
  }

  private void installBundle(String location, BufferedInputStream in,
      BufferedOutputStream out) throws IOException {
    try {
      Bundle bundle = framework.getBundleContext().getBundle(location);
      if (bundle != null) {
        bundle.update(in);
      } else {
        bundle = framework.getBundleContext().installBundle(location, in);
        bundle.start();
      }
      out.write("OK".getBytes());
    } catch (BundleException e) {
      out.write("FAIL".getBytes());
      e.printStackTrace();
    }
  }

}
