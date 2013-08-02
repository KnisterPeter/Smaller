package de.matrixweb.smaller.coffeescript;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.matrixweb.smaller.resource.Processor;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private CoffeescriptProcessor processor133;

  private CoffeescriptProcessor processor140;

  private CoffeescriptProcessor processor150;

  private CoffeescriptProcessor processor163;

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) {
    this.processor133 = registerService(context, "1.3.3", 133);
    this.processor140 = registerService(context, "1.4.0", 140);
    this.processor150 = registerService(context, "1.5.0", 150);
    this.processor163 = registerService(context, "1.6.3", 163);
  }

  private CoffeescriptProcessor registerService(final BundleContext context,
      final String version, final int ranking) {
    final CoffeescriptProcessor processor = new CoffeescriptProcessor(version);
    final Hashtable<String, Object> props = new Hashtable<String, Object>();
    props.put("name", "coffeeScript");
    props.put("version", version);
    props.put("service.ranking", Integer.valueOf(ranking));
    context.registerService(Processor.class, processor, props);
    return processor;
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext context) {
    this.processor133.dispose();
    this.processor140.dispose();
    this.processor150.dispose();
    this.processor163.dispose();
  }

}
