package de.matrixweb.smaller.osgi.maven.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.xml.sax.SAXException;

import de.matrixweb.smaller.osgi.Kernel;
import de.matrixweb.smaller.osgi.maven.MavenInstaller;

/**
 * @author markusw
 */
public class MavenInstallerImpl implements MavenInstaller {

  private final String repository;

  private final Framework framework;

  private final Map<String, Pom> current = new HashMap<String, Pom>();

  /**
   * @param repository
   * @param framework
   */
  public MavenInstallerImpl(final String repository, final Framework framework) {
    this.repository = repository;
    this.framework = framework;
  }

  /**
   * @see de.matrixweb.smaller.osgi.maven.MavenInstaller#installOrUpdate(java.lang.String)
   */
  @Override
  public void installOrUpdate(final String command) throws IOException {
    try {
      startOrUpdate(install(command), true);
    } catch (final BundleException e) {
      Kernel.log(e);
    }
  }

  /**
   * @param command
   * @return Returns a {@link Set} of {@link BundleTask}s to process after
   *         installation
   * @throws BundleException
   * @throws IOException
   */
  public Set<BundleTask> install(final String command) throws BundleException,
      IOException {
    final Set<BundleTask> tasks = new HashSet<MavenInstallerImpl.BundleTask>();

    final String[] parts = command.split(":");
    if ("mvn".equals(parts[0])) {
      Pom pom = new Pom(parts[1], parts[2], parts[3]);
      try {
        pom = resolvePom(pom);
        tasks.add(installBundle(pom.toURN(), pom));
        final List<String> embedded = getEmbeddedDependencies(tasks.iterator()
            .next().bundle);

        final List<Pom> requiredDependencies = new LinkedList<Pom>();
        for (final Pom dependecy : pom
            .resolveNearestDependencies(new Filter.CompoundFilter(
                new Filter.AcceptScopes("compile", "runtime"),
                new Filter.NotAcceptTypes("pom")))) {
          if (!embedded.contains(dependecy.toURN())) {
            requiredDependencies.add(dependecy);
          }
        }
        for (final Pom dep : requiredDependencies) {
          tasks.add(installBundle(dep.toURN(), dep));
        }
      } catch (final ParserConfigurationException e) {
        Kernel.log(e);
      }
    }

    return tasks;
  }

  /**
   * @param tasks
   * @param update
   * @throws BundleException
   * @throws IOException
   */
  public void startOrUpdate(final Set<BundleTask> tasks, final boolean update)
      throws BundleException, IOException {
    for (final BundleTask task : tasks) {
      if (task.bundle != null) {
        if (task.installed) {
          if (task.bundle.getHeaders().get(Constants.FRAGMENT_HOST) == null) {
            System.out.println("Starting bundle " + task.pom.toURN());
            task.bundle.start();
          }
        } else if (update) {
          final InputStream in = new URL(task.pom.toUrl(this.repository, "jar"))
              .openStream();
          try {
            System.out.println("Updating bundle " + task.pom.toURN());
            task.bundle.update(in);
          } finally {
            in.close();
          }
        }

      }
    }
  }

  private Pom resolvePom(final Pom pom) throws IOException,
      ParserConfigurationException {
    if (this.current.containsKey(pom.toURN())) {
      // Fast-Return recursive dependency declarations (managed dependencies)
      return this.current.get(pom.toURN());
    }
    this.current.put(pom.toURN(), pom);
    try {
      final InputStream is = new URL(pom.toUrl(this.repository, "pom"))
          .openStream();
      try {
        SAXParserFactory.newInstance().newSAXParser()
            .parse(is, new PomParser(pom));
        if (pom.getParent() != null) {
          pom.setParent(resolvePom(pom.getParent()));
        }
        final List<Pom> list = new ArrayList<Pom>(pom.getDependencies());
        pom.clearDependencies();
        for (final Pom dependency : list) {
          dependency.updateAfterParentResolved();
          pom.addDependency(resolvePom(dependency));
        }
        this.current.remove(pom.toURN());
      } finally {
        is.close();
      }
    } catch (final SAXException e) {
      // Skipping invalid pom
      System.out.println("Invalid pom " + pom.toURN() + " ... skipping");
      this.current.remove(pom.toURN());
    } catch (final FileNotFoundException e) {
      // Skipping missing pom
      // System.out.println("Missing pom " + pom.toURN() + " ... skipping");
      this.current.remove(pom.toURN());
    }
    return pom;
  }

  private BundleTask installBundle(final String location, final Pom pom)
      throws IOException, BundleException {
    final BundleTask task = new BundleTask();
    task.pom = pom;
    task.bundle = getBundle(location);
    if (task.bundle == null) {
      System.out.println("Installing bundle " + pom.toURN());
      final InputStream in = new URL(pom.toUrl(this.repository, "jar"))
          .openStream();
      try {
        task.bundle = this.framework.getBundleContext().installBundle(location,
            in);
        task.installed = true;
      } finally {
        in.close();
      }
    }
    return task;
  }

  private Bundle getBundle(final String location) {
    for (final Bundle bundle : this.framework.getBundleContext().getBundles()) {
      if (bundle.getLocation().equals(location)) {
        return bundle;
      }
    }
    return null;
  }

  private List<String> getEmbeddedDependencies(final Bundle bundle) {
    final List<String> list = new LinkedList<String>();
    final String embeddedArtifacts = (String) bundle.getHeaders().get(
        "Embedded-Artifacts");
    if (embeddedArtifacts != null) {
      final String embedded[] = embeddedArtifacts.split(",");
      for (final String def : embedded) {
        final String[] parts = def.split(";");
        final String groupId = parts[1].substring(3, parts[1].length() - 1);
        final String artifactId = parts[2].substring(3, parts[2].length() - 1);
        final String version = parts[3].substring(3, parts[3].length() - 1);
        list.add("mvn:" + groupId + ':' + artifactId + ':' + version);
      }
    }
    return list;
  }

  /** */
  public static class BundleTask {
    Pom pom;
    Bundle bundle;
    boolean installed = false;
  }

}
