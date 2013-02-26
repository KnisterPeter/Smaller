package de.matrixweb.smaller.lessjs;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.matrixweb.smaller.resource.Processor;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private LessjsProcessor processor130;

  private LessjsProcessor processorTrunk;

  private LessjsProcessor processor133;

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) {
    this.processor130 = new LessjsProcessor("1.3.0");
    this.processorTrunk = new LessjsProcessor("trunk");
    this.processor133 = new LessjsProcessor("1.3.3");

    Hashtable<String, Object> props = new Hashtable<String, Object>();
    props.put("name", "lessjs");
    props.put("version", "1.3.0");
    props.put("service.ranking", Integer.valueOf(10));
    context.registerService(Processor.class, this.processor130, props);

    props = new Hashtable<String, Object>();
    props.put("name", "lessjs");
    props.put("version", "trunk");
    // The trunk version must not have a high ranking so it is not choosed
    // automatically
    props.put("service.ranking", Integer.valueOf(9));
    context.registerService(Processor.class, this.processorTrunk, props);

    props = new Hashtable<String, Object>();
    props.put("name", "lessjs");
    props.put("version", "1.3.3");
    props.put("service.ranking", Integer.valueOf(13));
    context.registerService(Processor.class, this.processor133, props);
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext context) {
    this.processor130.dispose();
    this.processorTrunk.dispose();
    this.processor133.dispose();
  }

}
