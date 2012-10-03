package de.matrixweb.smaller.lessjs;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.matrixweb.smaller.resource.Processor;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) {
    Hashtable<String, Object> props = new Hashtable<String, Object>();
    props.put("name", "lessjs");
    props.put("version", "1.3.0");
    props.put("service.ranking", new Integer(10));
    context.registerService(Processor.class, new LessjsProcessor("1.3.0"),
        props);

    props = new Hashtable<String, Object>();
    props.put("name", "lessjs");
    props.put("version", "trunk");
    // The trunk version must not have a high ranking so it is not choosed
    // automatically
    props.put("service.ranking", new Integer(9));
    context.registerService(Processor.class, new LessjsProcessor("trunk"),
        props);
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext context) {
  }

}
