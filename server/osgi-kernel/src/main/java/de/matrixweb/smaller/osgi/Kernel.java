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

/**
 * @author markusw
 */
public class Kernel {

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
      log(e);
    } catch (final InterruptedException e) {
      log(e);
    } catch (final IOException e) {
      log(e);
    } catch (final Throwable t) {
      log(t);
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
    throw new RuntimeException("Missing 'repository' parameter");
  }

  private void run(final Framework framework, final String... args)
      throws IOException, InterruptedException {
    final MavenInstallerImpl maven = new MavenInstallerImpl(
        getRepository(args), framework);
    framework.getBundleContext().registerService(
        MavenInstaller.class.getName(), maven, null);
    installBundles(maven, args);
    framework.waitForStop(0);
  }

  private void installBundles(final MavenInstallerImpl maven,
      final String... args) throws IOException {
    try {
      final Set<BundleTask> tasks = new HashSet<MavenInstallerImpl.BundleTask>();
      for (final String arg : args) {
        if (arg.startsWith("mvn:")) {
          try {
            tasks.addAll(maven.install(arg));
          } catch (final IOException e) {
            log(e);
          }
        }
      }
      maven.startOrUpdate(tasks, false);
    } catch (final BundleException e) {
      log(e);
    }
  }

  /**
   * @param t
   *          {@link Throwable} to log
   */
  public static void log(final Throwable t) {
    t.printStackTrace();
  }

}
