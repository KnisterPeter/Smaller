package de.matrixweb.smaller.uglifyjs;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import de.matrixweb.smaller.resource.Processor;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private UglifyjsProcessor processor133;

  private UglifyjsProcessor processor243;

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) {
    this.processor133 = register(context, "1.3.3", 133);
    this.processor243 = register(context, "2.4.3", 243);
  }

  private UglifyjsProcessor register(final BundleContext context,
      final String version, final int ranking) {
    final UglifyjsProcessor processor = new UglifyjsProcessor(version);
    final Hashtable<String, Object> props = new Hashtable<String, Object>();
    props.put("name", "uglifyjs");
    props.put("version", version);
    props.put(Constants.SERVICE_RANKING, Integer.valueOf(ranking));
    context.registerService(Processor.class, processor, props);
    return processor;
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext context) {
    this.processor133.dispose();
    this.processor243.dispose();
  }

}
