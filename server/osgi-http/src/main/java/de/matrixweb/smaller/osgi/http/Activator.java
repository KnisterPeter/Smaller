package de.matrixweb.smaller.osgi.http;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.matrixweb.smaller.pipeline.Pipeline;
import de.matrixweb.smaller.resource.ProcessorFactory;
import de.matrixweb.smaller.resource.impl.OsgiServiceProcessorFactory;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

  private ProcessorFactory processorFactory;

  private Pipeline pipeline;

  private ServiceTracker<HttpService, HttpService> tracker;

  private final Set<HttpService> services = new HashSet<HttpService>();

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) throws ServletException {
    this.processorFactory = new OsgiServiceProcessorFactory(context);
    this.pipeline = new Pipeline(this.processorFactory);

    this.tracker = new ServiceTracker<HttpService, HttpService>(context,
        HttpService.class.getName(), null) {
      /**
       * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
       */
      @Override
      public HttpService addingService(
          final ServiceReference<HttpService> reference) {
        return registerServlet(super.addingService(reference));
      }

      /**
       * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference,
       *      java.lang.Object)
       */
      @Override
      public void removedService(final ServiceReference<HttpService> reference,
          final HttpService service) {
        service.unregister("/");
        Activator.this.services.remove(service);
        super.removedService(reference, service);
      }
    };
    this.tracker.open();
    final HttpService service = this.tracker.getService();
    registerServlet(service);
  }

  private HttpService registerServlet(final HttpService service) {
    if (service != null && !this.services.contains(service)) {
      try {
        service.registerServlet("/", new Servlet(this.pipeline), null, null);
        this.services.add(service);
      } catch (final ServletException e) {
        LOGGER.error("Failed to create SmallerServlet", e);
      } catch (final NamespaceException e) {
        LOGGER.error("Failed to register SmallerServlet on URL '/'", e);
      }
    }
    return service;
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext context) {
    for (final HttpService service : this.services) {
      service.unregister("/");
    }
    this.services.clear();
    if (this.tracker != null) {
      this.tracker.close();
      this.tracker = null;
    }
    if (this.processorFactory != null) {
      this.processorFactory.dispose();
      this.processorFactory = null;
    }
  }

}
