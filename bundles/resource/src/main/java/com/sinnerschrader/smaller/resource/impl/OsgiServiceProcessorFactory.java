package com.sinnerschrader.smaller.resource.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.sinnerschrader.smaller.resource.Processor;
import com.sinnerschrader.smaller.resource.ProcessorFactory;

/**
 * @author markusw
 */
public class OsgiServiceProcessorFactory implements ProcessorFactory {

  private BundleContext context;

  /**
   * 
   */
  public OsgiServiceProcessorFactory(BundleContext context) {
    this.context = context;
  }

  /**
   * @see com.sinnerschrader.smaller.resource.ProcessorFactory#getProcessor(java.lang.String)
   */
  @Override
  public Processor getProcessor(String name) {
    return getProcessor(name, null);
  }

  /**
   * @see com.sinnerschrader.smaller.resource.ProcessorFactory#getProcessor(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public Processor getProcessor(String name, String version) {
    Processor processor = null;
    try {
      String nameFilter = "(name=" + name + ")";
      String versionFilter = null;
      if (version != null) {
        versionFilter = "(version=" + version + ")";
      }
      String filter = null;
      if (versionFilter != null) {
        filter = "(&" + nameFilter + versionFilter + ")";
      } else {
        filter = name;
      }

      ServiceReference[] ref = context.getServiceReferences(
          Processor.class.getName(), filter);
      if (ref != null) {
        processor = (Processor) context.getService(ref[0]);
      }
    } catch (InvalidSyntaxException e) {
      e.printStackTrace();
    }
    return processor;
  }

}
