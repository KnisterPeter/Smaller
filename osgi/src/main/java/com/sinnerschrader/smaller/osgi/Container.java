package com.sinnerschrader.smaller.osgi;

import java.io.IOException;
import java.util.HashMap;
import java.util.ServiceLoader;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import com.sinnerschrader.smaller.osgi.maven.BundleInstaller;

/**
 * @author markusw
 */
public class Container {

  private Framework framework;

  private CommandListener connector;

  void run(String... args) {
    String repository = null;
    for (String arg : args) {
      if (arg.startsWith("-Drepository=")) {
        repository = arg.substring("-Drepository=".length());
      }
    }

    framework = ServiceLoader.load(FrameworkFactory.class).iterator().next()
        .newFramework(new HashMap<String, String>());
    startCommandListener(repository);
    try {
      framework.start();

      for (String arg : args) {
        if (arg.startsWith("mvn:")) {
          new BundleInstaller(repository, framework).install(arg);
        }
      }

      framework.waitForStop(0);
    } catch (BundleException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Throwable t) {
      t.printStackTrace();
    } finally {
      System.exit(0);
    }
  }

  private void startCommandListener(String repository) {
    if (repository.endsWith("/")) {
      repository = repository.substring(0, repository.length() - 1);
    }

    connector = new CommandListener(repository, framework);
    connector.setDaemon(true);
    connector.start();
  }

}
