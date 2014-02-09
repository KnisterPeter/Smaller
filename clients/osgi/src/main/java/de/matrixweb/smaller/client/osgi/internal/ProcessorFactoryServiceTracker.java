package de.matrixweb.smaller.client.osgi.internal;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import de.matrixweb.smaller.resource.ProcessorFactory;

/**
 * @author markusw
 */
public class ProcessorFactoryServiceTracker extends
    ServiceTracker<ProcessorFactory, ProcessorFactory> {

  private final List<ProcessorFactoryServiceListener> listeners = new ArrayList<ProcessorFactoryServiceTracker.ProcessorFactoryServiceListener>();

  private ProcessorFactory processorFactory;

  /**
   * @param context
   */
  public ProcessorFactoryServiceTracker(final BundleContext context) {
    super(context, ProcessorFactory.class, null);
  }

  /**
   * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
   */
  @Override
  public ProcessorFactory addingService(
      final ServiceReference<ProcessorFactory> reference) {
    final ProcessorFactory processorFactory = super.addingService(reference);
    this.processorFactory = processorFactory;
    for (final ProcessorFactoryServiceListener listener : this.listeners) {
      listener.addedProcessorFactory(processorFactory);
    }
    return processorFactory;
  }

  /**
   * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference,
   *      java.lang.Object)
   */
  @Override
  public void removedService(
      final ServiceReference<ProcessorFactory> reference,
      final ProcessorFactory service) {
    for (final ProcessorFactoryServiceListener listener : this.listeners) {
      listener.removedProcessorFactory(this.processorFactory);
    }
    this.processorFactory = null;
    super.removedService(reference, service);
  }

  /**
   * @param listener
   */
  public void addListener(final ProcessorFactoryServiceListener listener) {
    this.listeners.add(listener);
    if (this.processorFactory != null) {
      listener.addedProcessorFactory(this.processorFactory);
    }
  }

  /**
   * @param listener
   */
  public void removeListener(final ProcessorFactoryServiceListener listener) {
    this.listeners.remove(listener);
    if (this.processorFactory != null) {
      listener.removedProcessorFactory(this.processorFactory);
    }
  }

  /** */
  public static interface ProcessorFactoryServiceListener {

    /**
     * @param processorFactory
     */
    void addedProcessorFactory(ProcessorFactory processorFactory);

    /**
     * @param processorFactory
     */
    void removedProcessorFactory(ProcessorFactory processorFactory);

  }

}
