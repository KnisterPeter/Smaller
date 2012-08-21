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

  private final BundleContext context;

  /**
   * @param context
   */
  public OsgiServiceProcessorFactory(final BundleContext context) {
    this.context = context;
  }

  /**
   * @see com.sinnerschrader.smaller.resource.ProcessorFactory#getProcessor(java.lang.String)
   */
  @Override
  public Processor getProcessor(final String name) {
    return getProcessor(name, null);
  }

  /**
   * @see com.sinnerschrader.smaller.resource.ProcessorFactory#getProcessor(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public Processor getProcessor(final String name, final String version) {
    Processor processor = null;
    try {
      final String nameFilter = "(name=" + name + ")";
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

      final ServiceReference[] ref = this.context.getServiceReferences(
          Processor.class.getName(), filter);
      if (ref != null) {
        processor = (Processor) this.context.getService(ref[0]);
      }
    } catch (final InvalidSyntaxException e) {
      e.printStackTrace();
    }
    return processor;
  }

}
