package de.matrixweb.smaller.csso.internal;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import de.matrixweb.smaller.csso.CssoProcessor;
import de.matrixweb.smaller.resource.Processor;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private CssoProcessor processor139;

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) throws Exception {
    this.processor139 = register(context, "1.3.9", 139);
  }

  private CssoProcessor register(final BundleContext context,
      final String version, final int ranking) {
    final CssoProcessor processor = new CssoProcessor(version);
    final Hashtable<String, Object> props = new Hashtable<String, Object>();
    props.put("name", "csso");
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
    this.processor139.dispose();
  }

}
