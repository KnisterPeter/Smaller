package de.matrixweb.smaller.client.osgi;

import org.osgi.framework.Bundle;

import de.matrixweb.smaller.config.Environment;

/**
 * If this interface is implemented and registered as OSGi service it will be
 * used to decide on bundle inclusion strategy.
 * 
 * @author markusw
 */
public interface BundleSelector {

  /**
   * Returns true if the given {@link Bundle} should be included in the smaller
   * configuration for the given {@link Environment}.
   * 
   * @param environment
   *          The {@link Environment} to setup
   * @param bundle
   *          The {@link Bundle} to consider for inclusion
   * @return Returns true if the given bundle should be included, false
   *         otherwise
   */
  boolean shouldInclude(Environment environment, Bundle bundle);

}
