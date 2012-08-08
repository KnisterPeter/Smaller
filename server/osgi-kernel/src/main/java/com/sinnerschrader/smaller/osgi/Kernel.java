package com.sinnerschrader.smaller.osgi;

import java.io.IOException;
import java.util.HashMap;
import java.util.ServiceLoader;

import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import com.sinnerschrader.smaller.osgi.maven.MavenInstaller;
import com.sinnerschrader.smaller.osgi.maven.impl.MavenInstallerImpl;

/**
 * @author markusw
 */
public class Kernel {

  /**
   * @param args
   */
  public static void main(String... args) {
    new Kernel().start(args);
  }

  private void start(String... args) {
    HashMap<String, String> config = new HashMap<String, String>();
    config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
        "com.sinnerschrader.smaller.osgi.maven");
    Framework framework = ServiceLoader.load(FrameworkFactory.class).iterator()
        .next().newFramework(config);
    try {
      framework.start();
      run(framework, args);
    } catch (BundleException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Throwable t) {
      t.printStackTrace();
    } finally {
      System.exit(0);
    }
  }

  private String getRepository(String... args) {
    String repository = null;
    for (String arg : args) {
      if (arg.startsWith("-repository=")) {
        repository = arg.substring("-repository=".length());
        if (repository.endsWith("/")) {
          repository = repository.substring(0, repository.length() - 1);
        }
        return repository;
      }
    }
    throw new RuntimeException("Missing 'repository' parameter");
  }

  private void run(Framework framework, String... args) throws IOException,
      InterruptedException {
    MavenInstallerImpl maven = new MavenInstallerImpl(getRepository(args),
        framework);
    try {
      framework.getBundleContext().registerService(MavenInstaller.class, maven,
          null);
      for (String arg : args) {
        if (arg.startsWith("mvn:")) {
          maven.install(arg);
        }
      }
      framework.waitForStop(0);
    } finally {
    }
  }

}
