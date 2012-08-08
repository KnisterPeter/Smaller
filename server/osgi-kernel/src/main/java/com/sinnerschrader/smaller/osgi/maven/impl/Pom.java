package com.sinnerschrader.smaller.osgi.maven.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author markusw
 */
public class Pom extends Artifact {

  private Pom dependant;

  private Pom parent;

  private String packaging = "jar";

  private Map<String, String> properties = new HashMap<String, String>();

  private Map<String, Pom> managedDependencies = new HashMap<String, Pom>();

  private Map<String, Pom> dependencies = new HashMap<String, Pom>();

  private List<String> exclusions = new LinkedList<String>();

  /**
   * 
   */
  public Pom() {
  }

  /**
   * @param groupId
   * @param artifactId
   * @param version
   */
  public Pom(String groupId, String artifactId, String version) {
    super(groupId, artifactId, version);
    initProperties();
  }

  /**
   * @param copy
   */
  public Pom(Pom dependant, Pom copy) {
    super(copy);
    this.dependant = dependant;
    setPackaging(copy.getPackaging());
    exclusions.addAll(copy.exclusions);
    initProperties();
  }

  private void initProperties() {
    addProperty("project.groupId", getGroupId());
    addProperty("pom.groupId", getGroupId());
    addProperty("project.artifactId", getArtifactId());
    addProperty("pom.artifactId", getArtifactId());
    addProperty("project.version", getVersion());
    addProperty("pom.version", getVersion());
  }

  void updateAfterParentResolved() {
    if (getVersion() == null && dependant != null) {
      Artifact artifact = dependant.getManagedDependencies().get(
          getGroupArtifactKey());
      setTemplate(artifact);
    }
  }

  /**
   * @see com.sinnerschrader.smaller.osgi.maven.impl.Artifact#getGroupId()
   */
  @Override
  public String getGroupId() {
    return resolveProperties(super.getGroupId());
  }

  /**
   * @see com.sinnerschrader.smaller.osgi.maven.impl.Artifact#getArtifactId()
   */
  @Override
  public String getArtifactId() {
    return resolveProperties(super.getArtifactId());
  }

  /**
   * @see com.sinnerschrader.smaller.osgi.maven.impl.Artifact#getVersion()
   */
  @Override
  public String getVersion() {
    return resolveProperties(super.getVersion());
  }

  private String resolveProperties(String input) {
    if (input != null) {
      int start = input.indexOf("${");
      if (start > -1) {
        int pos = 0;
        StringBuilder sb = new StringBuilder();
        while (start > -1) {
          int end = input.indexOf("}", start);
          String match = input.substring(start + 2, end);
          String replacement = getReplacement(match);
          if (replacement != null) {
            sb.append(input.substring(pos, start)).append(replacement);
          } else {
            sb.append(input.substring(pos, end + 1));
          }
          pos = end + 1;
          start = input.indexOf("${", pos);
        }
        String done = sb.toString();
        if (!done.equals(input)) {
          input = resolveProperties(sb.toString());
        } else {
          input = done;
        }
      }
    }
    return input;
  }

  protected String getReplacement(String name) {
    String replacement = getProperties().get(name);
    if (replacement == null && dependant != null) {
      replacement = dependant.getReplacement(name);
    }
    return replacement;
  }

  /**
   * @return the parent
   */
  public Pom getParent() {
    return parent;
  }

  /**
   * @param parent
   *          the parent to set
   */
  public void setParent(Pom parent) {
    this.parent = parent;
  }

  /**
   * @return the packaging
   */
  public String getPackaging() {
    return packaging;
  }

  /**
   * @param packaging
   *          the packaging to set
   */
  public void setPackaging(String packaging) {
    this.packaging = packaging;
  }

  /**
   * @param managedDependencies
   *          the managedDependencies to set
   */
  public void addManagedDependency(Pom managedDependency) {
    this.managedDependencies.put(managedDependency.getGroupArtifactKey(),
        managedDependency);
  }

  /**
   * @return the managedDependencies
   */
  public Map<String, Pom> getManagedDependencies() {
    Map<String, Pom> deps = new HashMap<String, Pom>();
    if (getParent() != null) {
      deps.putAll(getParent().getManagedDependencies());
    }
    deps.putAll(managedDependencies);
    return deps;
  }

  /**
   * @return the dependencies
   */
  public Collection<Pom> getDependencies() {
    return dependencies.values();
  }

  private Set<String> getFilteredDependencies(boolean transitive, Filter filter) {
    Set<String> set = new HashSet<String>();
    if (filter.accept(this)) {
      set.add(getGroupArtifactKey());
    }
    set.addAll(internalGetFilteredDependencies(transitive,
        transitive ? new Filter.CompoundFilter(filter,
            new Filter.AcceptOptional(false)) : filter));
    return set;
  }

  private Set<String> internalGetFilteredDependencies(boolean transitive,
      Filter filter) {
    Set<String> set = new HashSet<String>();
    List<String> excl = getAllExclusions();
    for (Pom pom : getDependenciesIncludingParent()) {
      if (!excl.contains(pom.getGroupArtifactKey()) && filter.accept(pom)) {
        set.add(pom.getGroupArtifactKey());
        if (transitive) {
          set.addAll(pom.internalGetFilteredDependencies(transitive, filter));
        }
      }
    }
    set.removeAll(excl);
    return set;
  }

  /**
   * @param filter
   * @return
   */
  public Set<Pom> resolveNearestDependencies(Filter filter) {
    Set<Pom> set = new HashSet<Pom>();
    for (String candidate : getFilteredDependencies(true, filter)) {
      Queue<Pom> nodes = new ConcurrentLinkedQueue<Pom>();
      nodes.add(this);
      Pom result = findNearestDependency(nodes, candidate);
      if (result != null && filter.accept(result)) {
        set.add(result);
      }
    }
    return set;
  }

  private Pom findNearestDependency(Queue<Pom> nodes, String candidate) {
    while (!nodes.isEmpty()) {
      Pom node = nodes.remove();
      if (candidate.equals(node.getGroupArtifactKey())) {
        return node;
      }
      for (Pom dependency : node.getDependenciesIncludingParent()) {
        if (dependency != node) {
          nodes.add(dependency);
        }
      }
    }
    return null;
  }

  private List<Pom> getDependenciesIncludingParent() {
    List<Pom> list = new ArrayList<Pom>();
    list.addAll(getDependencies());
    if (getParent() != null) {
      list.addAll(getParent().getDependencies());
    }
    return list;
  }

  /**
   * @param dependencies
   *          the dependencies to set
   */
  public void addDependency(Pom dependency) {
    this.dependencies.put(dependency.getGroupArtifactKey(), dependency);
  }

  /**
   * @return the dependencies
   */
  void clearDependencies() {
    dependencies.clear();
  }

  private List<String> getAllExclusions() {
    List<String> list = new ArrayList<String>();
    list.addAll(exclusions);
    if (dependant != null) {
      list.addAll(dependant.getAllExclusions());
    }
    return list;
  }

  /**
   * @param exclusions
   *          the exclusions to set
   */
  public void addExclusion(String exclusion) {
    this.exclusions.add(exclusion);
  }

  /**
   * @return the properties
   */
  public Map<String, String> getProperties() {
    Map<String, String> props = new HashMap<String, String>();
    if (getParent() != null) {
      props.putAll(getParent().getProperties());
    }
    props.putAll(properties);
    return props;
  }

  /**
   * @param properties
   *          the properties to set
   */
  public void addProperty(String name, String value) {
    this.properties.put(name, value);
  }

  public String getGroupArtifactKey() {
    StringBuilder sb = new StringBuilder();
    sb.append(getGroupId()).append(':').append(getArtifactId());
    if (!"jar".equals(getType())) {
      sb.append("::").append(getType());
    }
    return sb.toString();
  }

  /**
   * @param repository
   * @param type
   * @return
   */
  public String toUrl(String repository) {
    return toUrl(repository, getPackaging());
  }

  /**
   * @param repository
   * @param type
   * @return
   */
  public String toUrl(String repository, String type) {
    return repository + '/' + getGroupId().replace('.', '/') + '/'
        + getArtifactId() + '/' + getVersion() + '/' + getArtifactId() + '-'
        + getVersion() + '.' + type;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    String urn = toURN();
    result = prime * result + ((urn == null) ? 0 : urn.hashCode());
    return result;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Pom other = (Pom) obj;
    if (!toURN().equals(other.toURN()))
      return false;
    return true;
  }

  public String dump(int level) {
    return dump(level, new Filter.AcceptAll());
  }

  public String dump(int level, Filter filter) {
    StringBuilder indent = new StringBuilder();
    for (int i = 0; i < level; i++) {
      indent.append("  ");
    }

    StringBuilder sb = new StringBuilder(indent).append(toString());
    for (String gak : getFilteredDependencies(false, filter)) {
      Pom pom = dependencies.get(gak);
      if (pom != null) {
        sb.append("\n").append(indent).append(pom.dump(level + 1, filter));
      }
    }
    return sb.toString();
  }

  /**
   * @see com.sinnerschrader.smaller.osgi.maven.impl.Artifact#toString()
   */
  @Override
  public String toString() {
    return toURN() + " [" + getScope() + (isOptional() ? '*' : "") + "]";
  }

}
