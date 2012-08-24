package de.matrixweb.smaller.osgi.maven.impl;

/**
 * @author markusw
 */
public class Artifact {

  private String groupId;

  private String artifactId;

  private String version;

  private String type;

  private String scope;

  private Boolean optional;

  private Artifact template;

  /**
   * 
   */
  public Artifact() {
  }

  /**
   * @param groupId
   * @param artifactId
   * @param version
   */
  public Artifact(final String groupId, final String artifactId,
      final String version) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
  }

  protected Artifact(final Artifact copy) {
    this.groupId = copy.groupId;
    this.artifactId = copy.artifactId;
    this.version = copy.version;
    this.type = copy.type;
    this.scope = copy.scope;
    this.optional = copy.optional;
  }

  /**
   * @return the groupId
   */
  public String getGroupId() {
    return this.groupId;
  }

  /**
   * @param groupId
   *          the groupId to set
   */
  public void setGroupId(final String groupId) {
    this.groupId = groupId;
  }

  /**
   * @return the artifactId
   */
  public String getArtifactId() {
    return this.artifactId;
  }

  /**
   * @param artifactId
   *          the artifactId to set
   */
  public void setArtifactId(final String artifactId) {
    this.artifactId = artifactId;
  }

  /**
   * @return the version
   */
  public String getVersion() {
    String v = this.version;
    if (v == null && this.template != null) {
      v = this.template.getVersion();
    }
    return v;
  }

  /**
   * @param version
   *          the version to set
   */
  public void setVersion(final String version) {
    this.version = version;
  }

  /**
   * @return the type
   */
  public String getType() {
    String t = this.type;
    if (t == null && this.template != null) {
      t = this.template.getType();
    }
    if (t == null) {
      t = "jar";
    }
    return t;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType(final String type) {
    this.type = type;
  }

  /**
   * @return the scope
   */
  public String getScope() {
    String s = this.scope;
    if (s == null && this.template != null) {
      s = this.template.getScope();
    }
    if (s == null) {
      s = "compile";
    }
    return s;
  }

  /**
   * @param scope
   *          the scope to set
   */
  public void setScope(final String scope) {
    this.scope = scope;
  }

  /**
   * @return the optional
   */
  public Boolean isOptional() {
    Boolean o = this.optional;
    if (o == null && this.template != null) {
      o = this.template.isOptional();
    }
    if (o == null) {
      o = Boolean.FALSE;
    }
    return o;
  }

  /**
   * @param optional
   *          the optional to set
   */
  public void setOptional(final Boolean optional) {
    this.optional = optional;
  }

  /**
   * 
   */
  public void clear() {
    this.groupId = null;
    this.artifactId = null;
    this.version = null;
    this.type = null;
    this.scope = null;
    this.optional = null;
  }

  /**
   * @param template
   *          the template to set
   */
  public void setTemplate(final Artifact template) {
    this.template = template;
  }

  /**
   * @return Returns the urn for this artifact
   */
  public String toURN() {
    final StringBuilder sb = new StringBuilder("mvn:").append(getGroupId())
        .append(':').append(getArtifactId()).append(':').append(getVersion());
    if (!"jar".equals(getType())) {
      sb.append(':').append(getType());
    }
    return sb.toString();
  }

}
