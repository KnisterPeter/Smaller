package com.sinnerschrader.smaller.sogi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * @author markusw
 */
public class Server {

  /**
   * @param args
   */
  public static void main(String[] args) {
    Map<String, String> configuration = new HashMap<String, String>();

    FrameworkFactory factory = ServiceLoader.load(FrameworkFactory.class)
        .iterator().next();
    Framework framework = factory.newFramework(configuration);
    try {
      framework.start();

      BundleContext bundleContext = framework.getBundleContext();
      List<Bundle> bundles = new LinkedList<Bundle>();
      for (String arg : args) {
        if (!arg.startsWith("-D")) {
          bundles.add(bundleContext.installBundle("file:" + arg));
        }
      }
      for (Bundle bundle : bundles) {
        bundle.start();
      }

      framework.waitForStop(0);
    } catch (BundleException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      System.exit(0);
    }
  }

}
