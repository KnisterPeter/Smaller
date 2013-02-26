package de.matrixweb.smaller.cssembed;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import de.matrixweb.smaller.resource.Processor;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private ServiceRegistration<Processor> registration;

  private CssembedProcessor processor;

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) {
    this.processor = new CssembedProcessor();

    final Hashtable<String, Object> props = new Hashtable<String, Object>();
    props.put("name", "cssembed");
    props.put("version", "0.4.5");
    props.put("service.ranking", Integer.valueOf(10));
    this.registration = context.registerService(Processor.class,
        this.processor, props);
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext context) {
    if (this.registration != null) {
      this.registration.unregister();
      this.registration = null;
    }
    this.processor.dispose();
    this.processor = null;
  }

}
