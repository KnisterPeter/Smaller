package de.matrixweb.smaller.osgi;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import de.matrixweb.smaller.osgi.maven.MavenInstaller;
import de.matrixweb.smaller.osgi.maven.impl.MavenInstallerImpl;
import de.matrixweb.smaller.osgi.maven.impl.MavenInstallerImpl.BundleTask;
import de.matrixweb.smaller.osgi.utils.Logger;

/**
 * @author markusw
 */
public final class Kernel {

  private Kernel() {
  }

  /**
   * @param args
   */
  public static void main(final String... args) {
    new Kernel().start(args);
  }

  private void start(final String... args) {
    final HashMap<String, String> config = new HashMap<String, String>();
    config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
        "de.matrixweb.smaller.osgi.maven");
    final Framework framework = ServiceLoader.load(FrameworkFactory.class)
        .iterator().next().newFramework(config);
    try {
      framework.start();
      run(framework, args);
    } catch (final BundleException e) {
      Logger.log(e);
    } catch (final InterruptedException e) {
      Logger.log(e);
    } catch (final IOException e) {
      Logger.log(e);
    } catch (final Throwable t) {
      Logger.log(t);
    } finally {
      System.exit(0);
    }
  }

  private String getRepository(final String... args) {
    String repository = null;
    for (final String arg : args) {
      if (arg.startsWith("-repository=")) {
        repository = arg.substring("-repository=".length());
        if (repository.endsWith("/")) {
          repository = repository.substring(0, repository.length() - 1);
        }
        return repository;
      }
    }
    throw new KernelException("Missing 'repository' parameter");
  }

  private void run(final Framework framework, final String... args)
      throws IOException, InterruptedException {
    final MavenInstallerImpl maven = new MavenInstallerImpl(
        getRepository(args), framework);
    framework.getBundleContext().registerService(MavenInstaller.class, maven,
        null);
    installBundles(framework, maven, args);
    framework.waitForStop(0);
  }

  private void installBundles(final Framework framework,
      final MavenInstallerImpl maven, final String... args) throws IOException {
    try {
      final Set<BundleTask> tasks = new HashSet<MavenInstallerImpl.BundleTask>();
      for (final String arg : args) {
        if (arg.startsWith("mvn:")) {
          try {
            tasks.addAll(maven.install(arg));
          } catch (final IOException e) {
            Logger.log(e);
          }
        } else if (arg.startsWith("install:")) {
          framework.getBundleContext()
              .installBundle("file:" + arg.substring("install:".length()))
              .start();
        }
      }
      maven.startOrUpdate(tasks, false);
    } catch (final BundleException e) {
      Logger.log(e);
    }
  }

  private static class KernelException extends RuntimeException {

    private static final long serialVersionUID = -5320057542448289703L;

    /**
     * @param message
     */
    public KernelException(final String message) {
      super(message);
    }

  }

}
