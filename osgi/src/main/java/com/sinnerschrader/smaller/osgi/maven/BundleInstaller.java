package com.sinnerschrader.smaller.osgi.maven;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.xml.sax.SAXException;

/**
 * @author markusw
 */
public class BundleInstaller {

  private String repository;

  private Framework framework;

  private Map<String, Pom> current = new HashMap<String, Pom>();

  public BundleInstaller(String repository, Framework framework) {
    this.repository = repository;
    this.framework = framework;
  }

  public void install(String command) throws IOException {
    installOrUpdate(command, false);
  }

  public void installOrUpdate(String command) throws IOException {
    installOrUpdate(command, true);
  }

  private void installOrUpdate(String command, boolean update)
      throws IOException {
    String[] parts = command.split(":");
    if ("mvn".equals(parts[0])) {
      Pom pom = new Pom(parts[1], parts[2], parts[3]);
      try {
        pom = resolvePom(pom);
        try {
          List<BundleTask> tasks = new LinkedList<BundleInstaller.BundleTask>();
          tasks.add(installBundle(pom.toURN(), pom));
          List<String> embedded = getEmbeddedDependencies(tasks.get(0).bundle);

          List<Pom> requiredDependencies = new LinkedList<Pom>();
          for (Pom dependecy : pom
              .resolveNearestDependencies(new Filter.CompoundFilter(
                  new Filter.AcceptScopes("compile", "runtime"),
                  new Filter.NotAcceptTypes("pom")))) {
            if (!embedded.contains(dependecy.toURN())) {
              requiredDependencies.add(dependecy);
            }
          }
          for (Pom dep : requiredDependencies) {
            tasks.add(installBundle(dep.toURN(), dep));
          }
          for (BundleTask task : tasks) {
            if (task.bundle != null) {
              if (task.installed) {
                task.bundle.start();
              } else if (update) {
                InputStream in = new URL(task.pom.toUrl(repository, "jar"))
                    .openStream();
                try {
                  task.bundle.update(in);
                } finally {
                  in.close();
                }
              }
            }
          }
        } catch (BundleException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      } catch (ParserConfigurationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  private Pom resolvePom(final Pom pom) throws IOException,
      ParserConfigurationException {
    if (current.containsKey(pom.toURN())) {
      // Fast-Return recursive dependency declarations (managed dependencies)
      return current.get(pom.toURN());
    }
    current.put(pom.toURN(), pom);
    try {
      InputStream is = new URL(pom.toUrl(repository, "pom")).openStream();
      try {
        SAXParserFactory.newInstance().newSAXParser()
            .parse(is, new PomParser(pom));
        if (pom.getParent() != null) {
          pom.setParent(resolvePom(pom.getParent()));
        }
        List<Pom> list = new ArrayList<Pom>(pom.getDependencies());
        pom.clearDependencies();
        for (Pom dependency : list) {
          dependency.updateAfterParentResolved();
          pom.addDependency(resolvePom(dependency));
        }
        current.remove(pom.toURN());
      } finally {
        is.close();
      }
    } catch (SAXException e) {
      // Skipping invalid pom
      System.out.println("Invalid pom " + pom.toURN() + " ... skipping");
      current.remove(pom.toURN());
    } catch (FileNotFoundException e) {
      // Skipping missing pom
      // System.out.println("Missing pom " + pom.toURN() + " ... skipping");
      current.remove(pom.toURN());
    }
    return pom;
  }

  private BundleTask installBundle(String location, Pom pom)
      throws IOException, BundleException {
    BundleTask task = new BundleTask();
    task.pom = pom;
    task.bundle = framework.getBundleContext().getBundle(location);
    if (task.bundle == null) {
      InputStream in = new URL(pom.toUrl(repository, "jar")).openStream();
      try {
        task.bundle = framework.getBundleContext().installBundle(location, in);
        task.installed = true;
      } finally {
        in.close();
      }
    }
    return task;
  }

  private List<String> getEmbeddedDependencies(Bundle bundle) {
    List<String> list = new LinkedList<String>();
    String embeddedArtifacts = bundle.getHeaders().get("Embedded-Artifacts");
    if (embeddedArtifacts != null) {
      String embedded[] = embeddedArtifacts.split(",");
      for (String def : embedded) {
        String[] parts = def.split(";");
        String groupId = parts[1].substring(3, parts[1].length() - 1);
        String artifactId = parts[2].substring(3, parts[2].length() - 1);
        String version = parts[3].substring(3, parts[3].length() - 1);
        list.add("mvn:" + groupId + ':' + artifactId + ':' + version);
      }
    }
    return list;
  }

  private static class BundleTask {
    Pom pom;
    Bundle bundle;
    boolean installed = false;
  }

}
