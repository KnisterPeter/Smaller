package com.sinnerschrader.smaller.osgi.telnetd;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.sinnerschrader.smaller.osgi.maven.MavenInstaller;

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
  public void start(BundleContext context) throws Exception {
    ServiceReference ref = context.getServiceReference(MavenInstaller.class
        .getName());
    if (ref != null) {
      MavenInstaller maven = (MavenInstaller) context.getService(ref);
      telnetd = new CommandListener(maven);
    }
    tracker = new ServiceTracker(context, MavenInstaller.class.getName(), null) {
      @Override
      public Object addingService(ServiceReference reference) {
        MavenInstaller maven = (MavenInstaller) super.addingService(reference);
        if (telnetd == null) {
          telnetd = new CommandListener(maven);
        }
        return maven;
      }
    };
    tracker.open();
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    tracker.close();
    if (telnetd != null) {
      telnetd.interrupt();
      telnetd = null;
    }
  }

}
