package de.matrixweb.smaller.resource.impl;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

import de.matrixweb.smaller.resource.vfs.internal.VFSManager.VFSURLStreamHandler;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private static class VFSURLStreamHandlerService extends
      AbstractURLStreamHandlerService {

    private final VFSURLStreamHandler handler = new VFSURLStreamHandler();

    /**
     * @see org.osgi.service.url.AbstractURLStreamHandlerService#openConnection(java.net.URL)
     */
    @Override
    public URLConnection openConnection(final URL u) throws IOException {
      return this.handler.openConnection(u);
    }

  }

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) throws Exception {
    final Hashtable<String, Object> props = new Hashtable<String, Object>();
    props.put(URLConstants.URL_HANDLER_PROTOCOL, "vfs");
    context.registerService(URLStreamHandlerService.class,
        new VFSURLStreamHandlerService(), props);
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext context) throws Exception {
  }

}
