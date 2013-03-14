package de.matrixweb.smaller.osgi.file;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import de.matrixweb.osgi.kernel.maven.MavenInstaller;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private ServiceTracker<MavenInstaller, MavenInstaller> tracker;

  private Watchdog watchdog;

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) throws Exception {
    String deployDir = context.getProperty("deploy.dir");
    if (deployDir == null) {
      deployDir = "deploy";
    }
    this.tracker = new ServiceTracker<MavenInstaller, MavenInstaller>(context,
        MavenInstaller.class, null);
    this.tracker.open();
    this.watchdog = new Watchdog(deployDir, context, this.tracker);
    this.watchdog.start();
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext context) throws Exception {
    this.watchdog.halt();
    this.tracker.close();
  }

}
