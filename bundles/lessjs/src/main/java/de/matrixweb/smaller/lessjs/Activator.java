package de.matrixweb.smaller.lessjs;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import de.matrixweb.smaller.resource.Processor;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private LessjsProcessor processor130;

  private LessjsProcessor processorTrunk;

  private LessjsProcessor processor133;

  private LessjsProcessor processor140;

  private LessjsProcessor processor141;

  private LessjsProcessor processor142;

  private LessjsProcessor processor150;

  private LessjsProcessor processor161;

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) {
    this.processor130 = registerLessVersion(context, "1.3.0", 130,
        new LessjsProcessor("1.3.0"));
    this.processorTrunk = registerLessVersion(context, "trunk", 132,
        new LessjsProcessor("trunk"));
    this.processor133 = registerLessVersion(context, "1.3.3", 133,
        new LessjsProcessor("1.3.3"));
    this.processor140 = registerLessVersion(context, "1.4.0", 140,
        new LessjsProcessor("1.4.0"));
    this.processor141 = registerLessVersion(context, "1.4.1", 141,
        new LessjsProcessor("1.4.1"));
    this.processor142 = registerLessVersion(context, "1.4.2", 142,
        new LessjsProcessor("1.4.2"));
    this.processor150 = registerLessVersion(context, "1.5.0", 150,
        new LessjsProcessor("1.5.0"));
    this.processor161 = registerLessVersion(context, "1.6.1", 161,
        new LessjsProcessor("1.6.1"));
  }

  private LessjsProcessor registerLessVersion(final BundleContext context,
      final String version, final int ranking, final LessjsProcessor processor) {
    final Hashtable<String, Object> props = new Hashtable<String, Object>();
    props.put("name", "lessjs");
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
    this.processor130.dispose();
    this.processorTrunk.dispose();
    this.processor133.dispose();
    this.processor140.dispose();
    this.processor141.dispose();
    this.processor142.dispose();
    this.processor150.dispose();
    this.processor161.dispose();
  }

}
