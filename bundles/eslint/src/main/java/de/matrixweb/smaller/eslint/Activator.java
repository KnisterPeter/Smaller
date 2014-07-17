package de.matrixweb.smaller.eslint;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.matrixweb.smaller.resource.Processor;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private EslintProcessor processor074;


  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) {
    this.processor074 = createProcessor(context, "074", 074);
  }

  private EslintProcessor createProcessor(final BundleContext context,
      final String version, final int ranking) {
    final EslintProcessor processor = new EslintProcessor();
    final Hashtable<String, Object> props = new Hashtable<String, Object>();
    props.put("name", "eslint");
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
    this.processor074.dispose();
  }

}
