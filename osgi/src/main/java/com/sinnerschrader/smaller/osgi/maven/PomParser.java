package com.sinnerschrader.smaller.osgi.maven;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author markusw
 */
public class PomParser extends DefaultHandler {

  private Pom pom;

  private Pom dependency = new Pom();

  private Artifact exclusion = new Artifact();

  private StringBuilder content = new StringBuilder();
  private boolean inParent = false;
  private boolean inProperties = false;
  private boolean inDependencyManagement = false;
  private boolean inDependencies = false;
  private boolean inDependency = false;
  private boolean inExclusions = false;
  private boolean inExclusion = false;
  private boolean inBuild = false;
  private boolean inProfiles = false;

  /**
   * @param pom
   */
  public PomParser(Pom pom) {
    super();
    this.pom = pom;
  }

  @Override
  public void startElement(String uri, String localName, String qName,
      Attributes attributes) throws SAXException {
    content.setLength(0);
    if ("parent".equals(qName)) {
      inParent = true;
    } else if ("build".equals(qName)) {
      inBuild = true;
    } else if ("profiles".equals(qName)) {
      inProfiles = true;
    } else if ("properties".equals(qName)) {
      inProperties = true;
    } else if ("dependencyManagement".equals(qName)) {
      inDependencyManagement = true;
    } else if ("dependencies".equals(qName)) {
      inDependencies = true;
    } else if ("dependency".equals(qName)) {
      inDependency = true;
    } else if ("exclusions".equals(qName)) {
      inExclusions = true;
    } else if ("exclusion".equals(qName)) {
      inExclusion = true;
    }
  }

  /**
   * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
   */
  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    content.append(ch, start, length);
  }

  @Override
  public void endElement(String uri, String localName, String qName)
      throws SAXException {
    if (inParent) {
      if ("parent".equals(qName)) {
        inParent = false;
        pom.setParent(new Pom(pom, dependency));
        dependency.clear();
      } else if ("groupId".equals(qName)) {
        dependency.setGroupId(content.toString());
      } else if ("artifactId".equals(qName)) {
        dependency.setArtifactId(content.toString());
      } else if ("version".equals(qName)) {
        dependency.setVersion(content.toString());
      }
    } else if (inBuild) {
      if ("build".equals(qName)) {
        inBuild = false;
      }
    } else if (inProfiles) {
      if ("profiles".equals(qName)) {
        inProfiles = false;
      }
    } else if (inDependencyManagement) {
      if ("dependencyManagement".equals(qName)) {
        inDependencyManagement = false;
      } else if (inDependencies) {
        if ("dependencies".equals(qName)) {
          inDependencies = false;
        } else if (inDependency) {
          if ("dependency".equals(qName)) {
            inDependency = false;
            pom.addManagedDependency(new Pom(pom, dependency));
            dependency.clear();
          } else if (inExclusions) {
            if ("exclusions".equals(qName)) {
              inExclusions = false;
            } else if (inExclusion) {
              if ("exclusion".equals(qName)) {
                inExclusion = false;
                dependency.addExclusion(exclusion.getGroupId() + ':'
                    + exclusion.getArtifactId());
                exclusion.clear();
              } else if ("groupId".equals(qName)) {
                exclusion.setGroupId(content.toString());
              } else if ("artifactId".equals(qName)) {
                exclusion.setArtifactId(content.toString());
              }
            }
          } else if ("groupId".equals(qName)) {
            dependency.setGroupId(content.toString());
          } else if ("artifactId".equals(qName)) {
            dependency.setArtifactId(content.toString());
          } else if ("version".equals(qName)) {
            dependency.setVersion(content.toString());
          } else if ("type".equals(qName)) {
            dependency.setType(content.toString());
          } else if ("scope".equals(qName)) {
            dependency.setScope(content.toString());
          } else if ("optional".equals(qName)) {
            dependency.setOptional(Boolean.parseBoolean(content.toString()));
          }
        }
      }
    } else if (inDependencies) {
      if ("dependencies".equals(qName)) {
        inDependencies = false;
      } else if (inDependency) {
        if ("dependency".equals(qName)) {
          inDependency = false;
          pom.addDependency(new Pom(pom, dependency));
          dependency.clear();
        } else if (inExclusions) {
          if ("exclusions".equals(qName)) {
            inExclusions = false;
          } else if (inExclusion) {
            if ("exclusion".equals(qName)) {
              inExclusion = false;
              dependency.addExclusion(exclusion.getGroupId() + ':'
                  + exclusion.getArtifactId());
              exclusion.clear();
            } else if ("groupId".equals(qName)) {
              exclusion.setGroupId(content.toString());
            } else if ("artifactId".equals(qName)) {
              exclusion.setArtifactId(content.toString());
            }
          }
        } else if ("groupId".equals(qName)) {
          dependency.setGroupId(content.toString());
        } else if ("artifactId".equals(qName)) {
          dependency.setArtifactId(content.toString());
        } else if ("version".equals(qName)) {
          dependency.setVersion(content.toString());
        } else if ("type".equals(qName)) {
          dependency.setType(content.toString());
        } else if ("scope".equals(qName)) {
          dependency.setScope(content.toString());
        } else if ("optional".equals(qName)) {
          dependency.setOptional(Boolean.parseBoolean(content.toString()));
        }
      }
    } else if (inProperties) {
      if ("properties".equals(qName)) {
        inProperties = false;
      } else {
        pom.addProperty(qName, content.toString());
      }
    } else if ("packaging".equals(qName)) {
      pom.setPackaging(content.toString());
    }
  }

}
