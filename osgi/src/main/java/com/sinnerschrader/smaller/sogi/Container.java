package com.sinnerschrader.smaller.sogi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.UUID;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * @author markusw
 */
public class Container {

  private Framework framework;

  private Connector connector;

  void run(String... args) {
    framework = ServiceLoader.load(FrameworkFactory.class).iterator().next()
        .newFramework(new HashMap<String, String>());
    startConnector(args);
    try {
      framework.start();
      installBundles(framework.getBundleContext(), args);
      framework.waitForStop(0);
    } catch (BundleException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      System.exit(0);
    }
  }

  private void startConnector(String... args) {
    String secret = null;
    for (String arg : args) {
      if (arg.startsWith("-Dsecret=")) {
        secret = arg.substring("-Dsecret=".length());
      }
    }
    if (secret == null) {
      // Generate random secret to prevent installation of bundles without
      // secret
      secret = UUID.randomUUID().toString();
    }

    connector = new Connector(secret, framework);
    connector.setDaemon(true);
    connector.start();
  }

  private void installBundles(BundleContext bundleContext, String... args)
      throws BundleException {
    List<Bundle> bundles = new LinkedList<Bundle>();
    for (String arg : args) {
      if (!arg.startsWith("-D")) {
        bundles.add(bundleContext.installBundle("file:" + arg));
      }
    }
    for (Bundle bundle : bundles) {
      bundle.start();
    }
  }

}
