package de.matrixweb.smaller.jpegtran;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.matrixweb.smaller.resource.Processor;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) {
    final Hashtable<String, Object> props = new Hashtable<String, Object>();
    props.put("name", "jpegtran");
    props.put("version", "");
    props.put("service.ranking", new Integer(10));
    context.registerService(Processor.class, new JpegtranProcessor(), props);
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext context) {
  }

}
