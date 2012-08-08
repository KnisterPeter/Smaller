package com.sinnerschrader.smaller.osgi.maven.impl;

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
  public Artifact(String groupId, String artifactId, String version) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
  }
  
  protected Artifact(Artifact copy) {
    groupId = copy.groupId;
    artifactId = copy.artifactId;
    version = copy.version;
    type = copy.type;
    scope = copy.scope;
    optional = copy.optional;
  }

  /**
   * @return the groupId
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * @param groupId
   *          the groupId to set
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * @return the artifactId
   */
  public String getArtifactId() {
    return artifactId;
  }

  /**
   * @param artifactId
   *          the artifactId to set
   */
  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  /**
   * @return the version
   */
  public String getVersion() {
    String v = version;
    if (v == null && template != null) {
      v = template.getVersion();
    }
    return v;
  }

  /**
   * @param version
   *          the version to set
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * @return the type
   */
  public String getType() {
    String t = type;
    if (t == null && template != null) {
      t = template.getType();
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
  public void setType(String type) {
    this.type = type;
  }

  /**
   * @return the scope
   */
  public String getScope() {
    String s = scope;
    if (s == null && template != null) {
      s = template.getScope();
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
  public void setScope(String scope) {
    this.scope = scope;
  }

  /**
   * @return the optional
   */
  public Boolean isOptional() {
    Boolean o = optional;
    if (o == null && template != null) {
      o = template.isOptional();
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
  public void setOptional(Boolean optional) {
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
  public void setTemplate(Artifact template) {
    this.template = template;
  }

  public String toURN() {
    StringBuilder sb = new StringBuilder("mvn:").append(getGroupId()).append(':').append(getArtifactId()).append(':').append(getVersion());
    if (!"jar".equals(getType())) {
      sb.append(':').append(getType());
    }
    return sb.toString();
  }

}
