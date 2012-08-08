package com.sinnerschrader.smaller.osgi.http;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.servlet.ServletException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private ServiceTracker tracker;

  private Set<HttpService> services = new HashSet<HttpService>();

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @SuppressWarnings("rawtypes")
  @Override
  public void start(BundleContext context) throws Exception {
    tracker = new ServiceTracker(context, HttpService.class.getName(), null) {
      /**
       * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
       */
      @Override
      public Object addingService(ServiceReference reference) {
        HttpService service = (HttpService) super.addingService(reference);
        if (!services.contains(service)) {
          try {
            service.registerServlet("/", new Servlet(), new Hashtable(), null);
            services.add(service);
          } catch (ServletException e) {
            e.printStackTrace();
          } catch (NamespaceException e) {
            e.printStackTrace();
          }
        }
        return service;
      }

      /**
       * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference,
       *      java.lang.Object)
       */
      @Override
      public void removedService(ServiceReference reference, Object o) {
        HttpService service = (HttpService) o;
        service.unregister("/");
        services.remove(service);
        super.removedService(reference, service);
      }
    };
    tracker.open();
    ServiceReference ref = context.getServiceReference(HttpService.class
        .getName());
    if (ref != null) {
      HttpService service = (HttpService) context.getService(ref);
      service.registerServlet("/", new Servlet(), new Hashtable(), null);
      services.add(service);
    }
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    for (HttpService service : services) {
      service.unregister("/");
    }
    services.clear();
    if (tracker != null) {
      tracker.close();
      tracker = null;
    }
  }

}
