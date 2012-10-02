package de.matrixweb.smaller.resource.impl;

import java.util.Collection;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.ProcessorFactory;

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
   * @see de.matrixweb.smaller.resource.ProcessorFactory#getProcessor(java.lang.String)
   */
  @Override
  public Processor getProcessor(final String name) {
    return getProcessor(name, null);
  }

  /**
   * @see de.matrixweb.smaller.resource.ProcessorFactory#getProcessor(java.lang.String,
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

      final Collection<ServiceReference<Processor>> refs = this.context
          .getServiceReferences(Processor.class, filter);
      if (!refs.isEmpty()) {
        processor = this.context.getService(refs.iterator().next());
      }
    } catch (final InvalidSyntaxException e) {
      e.printStackTrace();
    }
    return processor;
  }

}
