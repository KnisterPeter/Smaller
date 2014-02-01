package de.matrixweb.smaller.jshint;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.matrixweb.smaller.resource.Processor;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private JshintProcessor processor110;

  private JshintProcessor processor243;

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) {
    this.processor110 = createProcessor(context, "1.1.0", 110);
    this.processor243 = createProcessor(context, "2.4.3", 243);
  }

  private JshintProcessor createProcessor(final BundleContext context,
      final String version, final int ranking) {
    final JshintProcessor processor = new JshintProcessor();
    final Hashtable<String, Object> props = new Hashtable<String, Object>();
    props.put("name", "jshint");
    props.put("version", version);
    props.put("service.ranking", ranking);
    context.registerService(Processor.class, processor, props);
    return processor;
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext context) {
    this.processor110.dispose();
    this.processor243.dispose();
  }

}
