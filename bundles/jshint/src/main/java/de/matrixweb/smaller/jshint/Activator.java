package de.matrixweb.smaller.jshint;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.matrixweb.smaller.resource.Processor;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private JshintProcessor processor;

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) {
    this.processor = new JshintProcessor();

    final Hashtable<String, Object> props = new Hashtable<String, Object>();
    props.put("name", "jshint");
    props.put("version", "1.1.0");
    props.put("service.ranking", Integer.valueOf(11));
    context.registerService(Processor.class, this.processor, props);
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext context) {
    this.processor.dispose();
  }

}
