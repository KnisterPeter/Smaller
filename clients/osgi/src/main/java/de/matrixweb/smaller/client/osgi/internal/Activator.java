package de.matrixweb.smaller.client.osgi.internal;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author markusw
 */
public class Activator implements BundleActivator, BundleListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

  private BundleContext context;

  private ProcessorFactoryServiceTracker processorFactoryServiceTracker;

  private final Map<Bundle, SmallerConfigurationInstance> smallerBundles = new HashMap<Bundle, SmallerConfigurationInstance>();

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  public void start(final BundleContext context) throws Exception {
    this.context = context;
    this.processorFactoryServiceTracker = new ProcessorFactoryServiceTracker(
        context);
    this.processorFactoryServiceTracker.open();

    for (final Bundle bundle : context.getBundles()) {
      if (bundle.getState() == Bundle.INSTALLED
          || bundle.getState() == Bundle.RESOLVED
          || bundle.getState() == Bundle.ACTIVE) {
        checkSmallerBundle(bundle);
      }
    }
    context.addBundleListener(this);
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  public void stop(final BundleContext context) throws Exception {
    context.removeBundleListener(this);

    for (final SmallerConfigurationInstance instance : this.smallerBundles
        .values()) {
      instance.dispose(this.processorFactoryServiceTracker);
    }
    this.smallerBundles.clear();

    if (this.processorFactoryServiceTracker != null) {
      this.processorFactoryServiceTracker.close();
      this.processorFactoryServiceTracker = null;
    }
  }

  /**
   * @see org.osgi.framework.BundleListener#bundleChanged(org.osgi.framework.BundleEvent)
   */
  public void bundleChanged(final BundleEvent event) {
    switch (event.getType()) {
    case BundleEvent.INSTALLED:
      checkSmallerBundle(event.getBundle());
      break;
    case BundleEvent.UNINSTALLED:
      if (this.smallerBundles.containsKey(event.getBundle())) {
        LOGGER.info("Smaller-Bundle removed: {}", event.getBundle());
        this.smallerBundles.remove(event.getBundle()).dispose(
            this.processorFactoryServiceTracker);
      }
      break;
    }
  }

  private void checkSmallerBundle(final Bundle bundle) {
    final String smallerConfig = bundle.getHeaders().get(
        SmallerConfigurationInstance.SMALLER_HEADER);
    if (smallerConfig != null) {
      LOGGER.info("New Smaller-Bundle found: {} [config={}]", bundle,
          smallerConfig);
      this.smallerBundles.put(bundle, new SmallerConfigurationInstance(
          this.context, bundle, smallerConfig,
          this.processorFactoryServiceTracker));
    }
  }

}
