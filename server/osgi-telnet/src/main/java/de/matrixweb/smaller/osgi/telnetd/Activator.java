package de.matrixweb.smaller.osgi.telnetd;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import de.matrixweb.smaller.osgi.maven.MavenInstaller;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private ServiceTracker tracker;

  private CommandListener telnetd;

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) {
    this.tracker = new ServiceTracker(context, MavenInstaller.class.getName(),
        null);
    this.tracker.open();
    this.telnetd = new CommandListener(this.tracker);
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext context) {
    if (this.telnetd != null) {
      this.telnetd.interrupt();
      this.telnetd = null;
    }
    this.tracker.close();
  }

}
