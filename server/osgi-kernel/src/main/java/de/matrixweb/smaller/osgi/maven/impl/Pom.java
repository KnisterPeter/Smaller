package de.matrixweb.smaller.osgi.maven.impl;

import java.net.URL;
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

  private final Map<String, String> properties = new HashMap<String, String>();

  private final Map<String, Pom> managedDependencies = new HashMap<String, Pom>();

  private final Map<String, Pom> dependencies = new HashMap<String, Pom>();

  private final List<String> exclusions = new LinkedList<String>();

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
  public Pom(final String groupId, final String artifactId, final String version) {
    super(groupId, artifactId, version);
    initProperties();
  }

  /**
   * @param dependant
   * @param copy
   */
  public Pom(final Pom dependant, final Pom copy) {
    super(copy);
    this.dependant = dependant;
    setPackaging(copy.getPackaging());
    this.exclusions.addAll(copy.exclusions);
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
    if (getVersion() == null && this.dependant != null) {
      final Artifact artifact = this.dependant.getManagedDependencies().get(
          getGroupArtifactKey());
      setTemplate(artifact);
    }
  }

  /**
   * @see de.matrixweb.smaller.osgi.maven.impl.Artifact#getGroupId()
   */
  @Override
  public String getGroupId() {
    return resolveProperties(super.getGroupId());
  }

  /**
   * @see de.matrixweb.smaller.osgi.maven.impl.Artifact#getArtifactId()
   */
  @Override
  public String getArtifactId() {
    return resolveProperties(super.getArtifactId());
  }

  /**
   * @see de.matrixweb.smaller.osgi.maven.impl.Artifact#getVersion()
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
        final StringBuilder sb = new StringBuilder();
        while (start > -1) {
          final int end = input.indexOf("}", start);
          final String match = input.substring(start + 2, end);
          final String replacement = getReplacement(match);
          if (replacement != null) {
            sb.append(input.substring(pos, start)).append(replacement);
          } else {
            sb.append(input.substring(pos, end + 1));
          }
          pos = end + 1;
          start = input.indexOf("${", pos);
        }
        final String done = sb.toString();
        if (!done.equals(input)) {
          input = resolveProperties(sb.toString());
        } else {
          input = done;
        }
      }
    }
    return input;
  }

  protected String getReplacement(final String name) {
    String replacement = getProperties().get(name);
    if (replacement == null && this.dependant != null) {
      replacement = this.dependant.getReplacement(name);
    }
    return replacement;
  }

  /**
   * @return the parent
   */
  public Pom getParent() {
    return this.parent;
  }

  /**
   * @param parent
   *          the parent to set
   */
  public void setParent(final Pom parent) {
    this.parent = parent;
  }

  /**
   * @return the packaging
   */
  public String getPackaging() {
    return this.packaging;
  }

  /**
   * @param packaging
   *          the packaging to set
   */
  public void setPackaging(final String packaging) {
    this.packaging = packaging;
  }

  /**
   * @param managedDependency
   *          the managedDependency to set
   */
  public void addManagedDependency(final Pom managedDependency) {
    this.managedDependencies.put(managedDependency.getGroupArtifactKey(),
        managedDependency);
  }

  /**
   * @return the managedDependencies
   */
  public Map<String, Pom> getManagedDependencies() {
    final Map<String, Pom> deps = new HashMap<String, Pom>();
    if (getParent() != null) {
      deps.putAll(getParent().getManagedDependencies());
    }
    deps.putAll(this.managedDependencies);
    return deps;
  }

  /**
   * @return the dependencies
   */
  public Collection<Pom> getDependencies() {
    return this.dependencies.values();
  }

  private Set<String> getFilteredDependencies(final boolean transitive,
      final Filter filter) {
    final Set<String> set = new HashSet<String>();
    if (filter.accept(this)) {
      set.add(getGroupArtifactKey());
    }
    set.addAll(internalGetFilteredDependencies(transitive,
        transitive ? new Filter.CompoundFilter(filter,
            new Filter.AcceptOptional(false)) : filter));
    return set;
  }

  private Set<String> internalGetFilteredDependencies(final boolean transitive,
      final Filter filter) {
    final Set<String> set = new HashSet<String>();
    final List<String> excl = getAllExclusions();
    for (final Pom pom : getDependenciesIncludingParent()) {
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
   * @return Returns a {@link Set} of {@link Pom}s which are the nearest
   *         dependencies of this {@link Pom}
   */
  public Set<Pom> resolveNearestDependencies(final Filter filter) {
    final Set<Pom> set = new HashSet<Pom>();
    for (final String candidate : getFilteredDependencies(true, filter)) {
      final Queue<Pom> nodes = new ConcurrentLinkedQueue<Pom>();
      nodes.add(this);
      final Pom result = findNearestDependency(nodes, candidate);
      if (result != null && filter.accept(result)) {
        set.add(result);
      }
    }
    return set;
  }

  private Pom findNearestDependency(final Queue<Pom> nodes,
      final String candidate) {
    while (!nodes.isEmpty()) {
      final Pom node = nodes.remove();
      if (candidate.equals(node.getGroupArtifactKey())) {
        return node;
      }
      for (final Pom dependency : node.getDependenciesIncludingParent()) {
        if (dependency != node) {
          nodes.add(dependency);
        }
      }
    }
    return null;
  }

  private List<Pom> getDependenciesIncludingParent() {
    final List<Pom> list = new ArrayList<Pom>();
    list.addAll(getDependencies());
    if (getParent() != null) {
      list.addAll(getParent().getDependencies());
    }
    return list;
  }

  /**
   * @param dependency
   *          the dependency to add
   */
  public void addDependency(final Pom dependency) {
    this.dependencies.put(dependency.getGroupArtifactKey(), dependency);
  }

  void clearDependencies() {
    this.dependencies.clear();
  }

  private List<String> getAllExclusions() {
    final List<String> list = new ArrayList<String>();
    list.addAll(this.exclusions);
    if (this.dependant != null) {
      list.addAll(this.dependant.getAllExclusions());
    }
    return list;
  }

  /**
   * @param exclusion
   *          the exclusion to add
   */
  public void addExclusion(final String exclusion) {
    this.exclusions.add(exclusion);
  }

  /**
   * @return the properties
   */
  public Map<String, String> getProperties() {
    final Map<String, String> props = new HashMap<String, String>();
    if (getParent() != null) {
      props.putAll(getParent().getProperties());
    }
    props.putAll(this.properties);
    return props;
  }

  /**
   * @param name
   * @param value
   */
  public void addProperty(final String name, final String value) {
    this.properties.put(name, value);
  }

  /**
   * @return Returns the group artifact key (e.g. used for depencency lookup)
   */
  public String getGroupArtifactKey() {
    final StringBuilder sb = new StringBuilder();
    sb.append(getGroupId()).append(':').append(getArtifactId());
    if (!"jar".equals(getType())) {
      sb.append("::").append(getType());
    }
    return sb.toString();
  }

  /**
   * @param repository
   * @return Returns the {@link Pom} {@link URL} locating it in the repository
   */
  public String toUrl(final String repository) {
    return toUrl(repository, getPackaging());
  }

  /**
   * @param repository
   * @param type
   * @return Returns the {@link Pom} {@link URL} locating it in the repository
   */
  public String toUrl(final String repository, final String type) {
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
    final String urn = toURN();
    result = prime * result + (urn == null ? 0 : urn.hashCode());
    return result;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Pom other = (Pom) obj;
    if (!toURN().equals(other.toURN())) {
      return false;
    }
    return true;
  }

  /**
   * @param level
   * @return Returns a {@link String} listing all dependencies
   */
  public String dump(final int level) {
    return dump(level, new Filter.AcceptAll());
  }

  /**
   * @param level
   * @param filter
   * @return Returns a {@link String} listing all {@link Filter} matching
   *         dependencies
   */
  public String dump(final int level, final Filter filter) {
    final StringBuilder indent = new StringBuilder();
    for (int i = 0; i < level; i++) {
      indent.append("  ");
    }

    final StringBuilder sb = new StringBuilder(indent).append(toString());
    for (final String gak : getFilteredDependencies(false, filter)) {
      final Pom pom = this.dependencies.get(gak);
      if (pom != null) {
        sb.append("\n").append(indent).append(pom.dump(level + 1, filter));
      }
    }
    return sb.toString();
  }

  /**
   * @see de.matrixweb.smaller.osgi.maven.impl.Artifact#toString()
   */
  @Override
  public String toString() {
    return toURN() + " [" + getScope() + (isOptional() ? '*' : "") + "]";
  }

}
