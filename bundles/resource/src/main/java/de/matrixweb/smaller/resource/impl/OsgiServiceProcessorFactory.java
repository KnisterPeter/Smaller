package de.matrixweb.smaller.resource.impl;

import java.util.Collection;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.ProcessorFactory;

/**
 * @author markusw
 */
public class OsgiServiceProcessorFactory implements ProcessorFactory {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(OsgiServiceProcessorFactory.class);

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
    if (name.indexOf(':') > -1) {
      final String[] parts = name.split(":");
      return getProcessor(parts[0], parts[1]);
    }
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
        filter = nameFilter;
      }

      final Collection<ServiceReference<Processor>> refs = this.context
          .getServiceReferences(Processor.class, filter);
      LOGGER.info("Found {} processors matching '{}'", refs.size(), filter);
      if (!refs.isEmpty()) {
        processor = this.context.getService(selectProcessor(refs));
      }
      if (processor == null) {
        throw new SmallerException("Failed to create processor " + name + "@"
            + version);
      }
    } catch (final InvalidSyntaxException e) {
      LOGGER.error("Failed to create processor " + name + "@" + version, e);
    }
    return processor;
  }

  private ServiceReference<Processor> selectProcessor(
      final Collection<ServiceReference<Processor>> refs) {
    int refRanking = 0;
    ServiceReference<Processor> ref = null;
    for (final ServiceReference<Processor> candidate : refs) {
      final int candidateRanking = (Integer) candidate
          .getProperty(Constants.SERVICE_RANKING);
      if (refRanking < candidateRanking) {
        refRanking = candidateRanking;
        ref = candidate;
      }
    }
    return ref;
  }

  /**
   * @see de.matrixweb.smaller.resource.ProcessorFactory#dispose()
   */
  @Override
  public void dispose() {
  }

}
