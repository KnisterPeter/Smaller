package de.matrixweb.smaller.client.osgi.internal;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.matrixweb.smaller.client.osgi.internal.ProcessorFactoryServiceTracker.ProcessorFactoryServiceListener;
import de.matrixweb.smaller.common.Manifest;
import de.matrixweb.smaller.common.ProcessDescription;
import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.config.ConfigFile;
import de.matrixweb.smaller.config.Environment;
import de.matrixweb.smaller.pipeline.Pipeline;
import de.matrixweb.smaller.resource.ProcessorFactory;
import de.matrixweb.vfs.VFS;
import de.matrixweb.vfs.wrapped.MergingVFS;
import de.matrixweb.vfs.wrapped.WrappedSystem;

/**
 * @author markusw
 */
public class SmallerConfigurationInstance implements
    ProcessorFactoryServiceListener {

  /**
   * The manifest header defining the location of the smaller config-file.
   */
  public static final String SMALLER_HEADER = "Smaller-Config";

  private static final Logger LOGGER = LoggerFactory
      .getLogger(SmallerConfigurationInstance.class);

  private final BundleContext bundleContext;

  private final Bundle smallerConfigBundle;

  private final String config;

  private Pipeline pipeline;

  private final List<ServiceHolder> services = new ArrayList<SmallerConfigurationInstance.ServiceHolder>();

  /**
   * @param bundleContext
   * @param smallerConfigBundle
   *          The {@link BundleContext} the the bundle containing the
   *          <code>Smaller-Config</code> header
   * @param config
   *          The value of the
   * @param tracker
   * 
   */
  public SmallerConfigurationInstance(final BundleContext bundleContext,
      final Bundle smallerConfigBundle, final String config,
      final ProcessorFactoryServiceTracker tracker) {
    this.bundleContext = bundleContext;
    this.smallerConfigBundle = smallerConfigBundle;
    this.config = config;

    tracker.addListener(this);
  }

  /**
   * @see de.matrixweb.smaller.client.osgi.internal.ProcessorFactoryServiceTracker.ProcessorFactoryServiceListener#addedProcessorFactory(de.matrixweb.smaller.resource.ProcessorFactory)
   */
  public void addedProcessorFactory(final ProcessorFactory processorFactory) {
    this.pipeline = new Pipeline(processorFactory);
    updateSerlvetStatus();
  }

  /**
   * @see de.matrixweb.smaller.client.osgi.internal.ProcessorFactoryServiceTracker.ProcessorFactoryServiceListener#removedProcessorFactory(de.matrixweb.smaller.resource.ProcessorFactory)
   */
  public void removedProcessorFactory(final ProcessorFactory processorFactory) {
    this.pipeline = null;
    updateSerlvetStatus();
  }

  private void updateSerlvetStatus() {
    if (this.pipeline != null) {
      LOGGER.info("Creating smaller pipeline and publishing servlet");
      registerServlets();
    } else {
      LOGGER.info("Disposing smaller pipeline and removing servlet");
      disposeServlets();
    }
  }

  private void registerServlets() {
    try {
      final ConfigFile configFile = ConfigFile.read(this.smallerConfigBundle
          .getResource(this.config));

      final Manifest manifest = Manifest.fromConfigFile(configFile);
      for (final ProcessDescription processDescription : manifest
          .getProcessDescriptions()) {
        LOGGER.info("Adding Smaller Servlet for URL '{}'",
            processDescription.getOutputFile());

        final ServiceHolder holder = new ServiceHolder();
        holder.vfs = new VFS();
        setupVfs(holder.vfs, getEnvironment(configFile, processDescription));
        holder.servlet = new Servlet(holder.vfs, this.pipeline,
            processDescription);
        final Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("alias", processDescription.getOutputFile());
        holder.servletService = this.bundleContext.registerService(
            javax.servlet.Servlet.class, holder.servlet, props);

        this.services.add(holder);
      }
    } catch (final IOException e) {
      throw new SmallerException("Failed to register smaller servlets", e);
    }
  }

  private Environment getEnvironment(final ConfigFile configFile,
      final ProcessDescription processDescription) {
    for (final Environment candidate : configFile.getEnvironments().values()) {
      if (candidate.getPipeline()[0].equals(processDescription.getProcessors()
          .get(0).getName())) {
        return candidate;
      }
    }
    return null;
  }

  private void setupVfs(final VFS vfs, final Environment env)
      throws IOException {
    final List<WrappedSystem> files = new ArrayList<WrappedSystem>();
    for (final Bundle bundle : this.bundleContext.getBundles()) {
      for (final String folder : env.getFiles().getFolder()) {
        final Enumeration<URL> urls = bundle.findEntries(folder, null, true);
        if (urls != null) {
          files.add(new OsgiBundleEntry(bundle, folder));
        }
      }
    }
    vfs.mount(vfs.find("/"), new MergingVFS(files));
  }

  private void disposeServlets() {
    for (final ServiceHolder holder : this.services) {
      if (holder.servletService != null) {
        holder.servletService.unregister();
        holder.servletService = null;
      }
      if (holder.servlet != null) {
        holder.servlet = null;
      }
      if (holder.vfs != null) {
        holder.vfs.dispose();
        holder.vfs = null;
      }
    }
    this.services.clear();
  }

  /**
   * @param tracker
   */
  public void dispose(final ProcessorFactoryServiceTracker tracker) {
    tracker.removeListener(this);
  }

  private static class ServiceHolder {

    private Servlet servlet;

    private ServiceRegistration<javax.servlet.Servlet> servletService;

    private VFS vfs;

  }

}
