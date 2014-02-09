package de.matrixweb.smaller.sweetjs.internal;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.sweetjs.SweetjsProcessor;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private SweetjsProcessor processor025;

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) throws Exception {
    this.processor025 = register(context, "0.2.5", 25);
  }

  private SweetjsProcessor register(final BundleContext context,
      final String version, final int ranking) {
    final SweetjsProcessor processor = new SweetjsProcessor(version);
    final Hashtable<String, Object> props = new Hashtable<String, Object>();
    props.put("name", "sweetjs");
    props.put("version", version);
    props.put(Constants.SERVICE_RANKING, Integer.valueOf(ranking));
    context.registerService(Processor.class, processor, props);
    return processor;
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext context) throws Exception {
    this.processor025.dispose();
  }

}
