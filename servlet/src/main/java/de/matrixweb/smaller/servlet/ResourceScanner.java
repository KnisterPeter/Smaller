package de.matrixweb.smaller.servlet;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

/**
 * @author marwol
 */
public class ResourceScanner {

  private ServletContext servletContext;

  private String[] includes;

  private String[] excludes;

  /**
   * @param servletContext
   * @param includes
   * @param excludes
   */
  public ResourceScanner(ServletContext servletContext, String[] includes, String[] excludes) {
    this.servletContext = servletContext;
    this.includes = includes;
    this.excludes = excludes;
  }

  /**
   * @return Returns a set of all resources matching the given includes and excludes definition
   */
  public Set<String> getResources() {
    Set<String> resources = new HashSet<String>();

    for (String include : includes) {
      resources.addAll(findCandidates("/", include));
    }
    for (String exclude : excludes) {
      for (String match : filterRecursive(resources, "/", exclude, !exclude.startsWith("**"))) {
        resources.remove(match);
      }
    }
    resources.remove(null);

    return resources;
  }

  private Set<String> findCandidates(String base, String include) {
    Set<String> candidates = new HashSet<String>();

    int starstar = include.indexOf("**");
    if (starstar > -1) {
      candidates.addAll(filterRecursive(base, include, starstar, starstar > 0));
    } else {
      int star = include.indexOf("*");
      if (star > -1) {
        candidates.addAll(filterRecursive(base, include, star, true));
      } else {
        candidates.add(findDirect(base, include));
      }
    }

    return candidates;
  }

  @SuppressWarnings("unchecked")
  private Set<String> findRecursive(String base) {
    Set<String> matches = new HashSet<String>();

    for (String match : (Set<String>) servletContext.getResourcePaths(base)) {
      if (match.endsWith("/")) {
        matches.addAll(findRecursive(match));
      } else {
        matches.add(match);
      }
    }

    return matches;
  }

  private Set<String> filterRecursive(String base, String include, int index, boolean prefixPatternWithBase) {
    return filterRecursive(findRecursive(base + include.substring(0, index)), base, include, prefixPatternWithBase);
  }

  private Set<String> filterRecursive(Set<String> matches, String base, String include, boolean prefixPatternWithBase) {
    Set<String> result = new HashSet<String>();

    String pattern = include.replace(".", "\\.").replace("**", "#starstar#").replace("*", "[^/]+").replace("#starstar#", ".*");
    if (prefixPatternWithBase) {
      pattern = base + pattern;
    }
    Pattern expr = Pattern.compile(pattern);
    for (String match : matches) {
      if (match != null && expr.matcher(match).matches()) {
        result.add(match);
      }
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  private String findDirect(String base, String include) {
    String parent = include.substring(0, include.lastIndexOf('/'));
    for (String match : (Set<String>) servletContext.getResourcePaths(base + parent)) {
      if (match.equals(base + include)) {
        return match;
      }
    }
    return null;
  }

}
