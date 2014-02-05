package de.matrixweb.smaller.browserify;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.matrixweb.smaller.resource.Processor;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private BrowserifyProcessor processor2340;

  private BrowserifyProcessor processor3249;

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) {
    this.processor2340 = registerService(context, "2.34.0", 2340);
    this.processor3249 = registerService(context, "3.24.9", 3249);
  }

  private BrowserifyProcessor registerService(final BundleContext context,
      final String version, final int ranking) {
    final BrowserifyProcessor processor = new BrowserifyProcessor(version);
    final Hashtable<String, Object> props = new Hashtable<String, Object>();
    props.put("name", "browserify");
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
    this.processor2340.dispose();
    this.processor3249.dispose();
  }

}
