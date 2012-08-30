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
import de.matrixweb.smaller.resource.impl.OsgiServiceProcessorFactory;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

  private Pipeline pipeline;

  private ServiceTracker tracker;

  private final Set<HttpService> services = new HashSet<HttpService>();

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) throws ServletException {
    this.pipeline = new Pipeline(new OsgiServiceProcessorFactory(context));

    this.tracker = new ServiceTracker(context, HttpService.class.getName(),
        null) {
      /**
       * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
       */
      @Override
      public Object addingService(final ServiceReference reference) {
        final HttpService service = (HttpService) super
            .addingService(reference);
        if (!Activator.this.services.contains(service)) {
          try {
            service.registerServlet("/", new Servlet(Activator.this.pipeline),
                null, null);
            Activator.this.services.add(service);
          } catch (final ServletException e) {
            LOGGER.error("Failed to create SmallerServlet", e);
          } catch (final NamespaceException e) {
            LOGGER.error("Failed to register SmallerServlet on URL '/'", e);
          }
        }
        return service;
      }

      /**
       * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference,
       *      java.lang.Object)
       */
      @Override
      public void removedService(final ServiceReference reference,
          final Object o) {
        final HttpService service = (HttpService) o;
        service.unregister("/");
        Activator.this.services.remove(service);
        super.removedService(reference, service);
      }
    };
    this.tracker.open();
    final HttpService service = (HttpService) this.tracker.getService();
    if (service != null) {
      try {
        service.registerServlet("/", new Servlet(this.pipeline), null, null);
        this.services.add(service);
      } catch (final NamespaceException e) {
        LOGGER.error("Failed to register SmallerServlet on URL '/'", e);
      }
    }
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
  }

}
