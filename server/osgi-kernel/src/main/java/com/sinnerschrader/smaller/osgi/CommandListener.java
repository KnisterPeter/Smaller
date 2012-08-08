package com.sinnerschrader.smaller.osgi;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.osgi.framework.launch.Framework;

import com.sinnerschrader.smaller.osgi.maven.MavenInstaller;

/**
 * @author markusw
 */
public class CommandListener extends Thread {

  private String repository;

  private Framework framework;

  CommandListener(String repository, Framework framework) {
    super();
    this.repository = repository;
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
      try {
        in = new BufferedInputStream(client.getInputStream());
        new MavenInstaller(repository, framework)
            .installOrUpdate(readCommand(in).trim());
      } finally {
        if (in != null) {
          in.close();
        }
        client.close();
      }
    } catch (IOException e) {
      // Client failed
      e.printStackTrace();
    }
  }

  private String readCommand(InputStream in) throws IOException {
    StringBuilder buf = new StringBuilder();
    char c = (char) in.read();
    while (c != '\n') {
      buf.append(c);
      c = (char) in.read();
    }
    return buf.toString();
  }

}
