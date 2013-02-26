package de.matrixweb.smaller.ycssmin;

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

  private YcssminProcessor processor;

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) {
    this.processor = new YcssminProcessor();

    final Hashtable<String, Object> props = new Hashtable<String, Object>();
    props.put("name", "ycssmin");
    props.put("version", "913e1945c2");
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
  }

}
