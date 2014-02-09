package de.matrixweb.smaller.svgo.internal;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.svgo.SvgoProcessor;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private SvgoProcessor processor037;

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) throws Exception {
    this.processor037 = register(context, "0.3.7", 37);
  }

  private SvgoProcessor register(final BundleContext context,
      final String version, final int ranking) {
    final SvgoProcessor processor = new SvgoProcessor(version);
    final Hashtable<String, Object> props = new Hashtable<String, Object>();
    props.put("name", "svgo");
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
    this.processor037.dispose();
  }

}
