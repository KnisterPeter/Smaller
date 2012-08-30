package de.matrixweb.smaller.osgi.telnetd;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
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
    final ServiceReference ref = context
        .getServiceReference(MavenInstaller.class.getName());
    if (ref != null) {
      final MavenInstaller maven = (MavenInstaller) context.getService(ref);
      this.telnetd = new CommandListener(maven);
    }
    this.tracker = new ServiceTracker(context, MavenInstaller.class.getName(),
        null) {
      @Override
      public Object addingService(final ServiceReference reference) {
        final MavenInstaller maven = (MavenInstaller) super
            .addingService(reference);
        if (Activator.this.telnetd == null) {
          Activator.this.telnetd = new CommandListener(maven);
        }
        return maven;
      }
    };
    this.tracker.open();
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext context) {
    this.tracker.close();
    if (this.telnetd != null) {
      this.telnetd.interrupt();
      this.telnetd = null;
    }
  }

}
