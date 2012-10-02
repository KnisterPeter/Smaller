package de.matrixweb.smaller.osgi.file;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.util.tracker.ServiceTracker;

import de.matrixweb.smaller.osgi.maven.MavenInstaller;

/**
 * @author markusw
 */
public class Watchdog extends Thread {

  private static final FileFilter JAR_FILE_FILTER = new FileFilter() {
    @Override
    public boolean accept(final File file) {
      return file.isFile() && file.getName().endsWith(".jar");
    }
  };

  private volatile boolean running = true;

  private final File directory;

  private final BundleContext context;

  private final ServiceTracker<MavenInstaller, MavenInstaller> tracker;

  private final Map<File, Long> lastUpdated = new HashMap<File, Long>();

  /**
   * @param directory
   * @param context
   * @param tracker
   * 
   */
  public Watchdog(final String directory, final BundleContext context,
      final ServiceTracker<MavenInstaller, MavenInstaller> tracker) {
    super("Smaller-FileInstaller-Watchdog");
    this.directory = new File(directory);
    this.context = context;
    this.tracker = tracker;
  }

  void halt() {
    this.running = false;
  }

  /**
   * @see java.lang.Thread#run()
   */
  @Override
  public void run() {
    boolean update = false;
    while (this.running) {
      try {
        // Install and update new file...
        final Collection<File> list = Arrays.asList(this.directory
            .listFiles(JAR_FILE_FILTER));
        final List<File> toBeInstalled = new ArrayList<File>();
        for (final File file : list) {
          if (!this.lastUpdated.containsKey(file)
              || this.lastUpdated.get(file) < file.lastModified()) {
            toBeInstalled.add(file);
            this.lastUpdated.put(file, file.lastModified());
          }
        }
        try {
          this.tracker.waitForService(0).installOrUpdate(update,
              toBeInstalled.toArray(new File[0]));
        } catch (final IOException e) {
          e.printStackTrace();
        }

        // ... and remove deleted ones
        final Collection<File> current = new ArrayList<File>(
            this.lastUpdated.keySet());
        current.removeAll(list);
        for (final File file : current) {
          final String location = file.toURI().toString();
          try {
            this.context.getBundle(location).uninstall();
            this.lastUpdated.remove(file);
          } catch (final BundleException e) {
            e.printStackTrace();
          }
        }

        update = true;
        Thread.sleep(1000 * 5);
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
