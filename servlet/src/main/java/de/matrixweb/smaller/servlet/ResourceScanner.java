package de.matrixweb.smaller.servlet;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

/**
 * @author marwol
 */
public class ResourceScanner {

  private final ServletContext servletContext;

  private final String[] includes;

  private final String[] excludes;

  /**
   * @param servletContext
   * @param includes
   * @param excludes
   */
  public ResourceScanner(final ServletContext servletContext,
      final String[] includes, final String[] excludes) {
    this.servletContext = servletContext;
    this.includes = includes;
    this.excludes = excludes;
  }

  /**
   * @return Returns a set of all resources matching the given includes and
   *         excludes definition
   */
  public Set<String> getResources() {
    final Set<String> resources = new HashSet<String>();

    for (final String include : this.includes) {
      resources.addAll(findCandidates("/", include));
    }
    for (final String exclude : this.excludes) {
      for (final String match : filterRecursive(resources, "/", exclude,
          !exclude.startsWith("**"))) {
        resources.remove(match);
      }
    }
    resources.remove(null);

    return resources;
  }

  private Set<String> findCandidates(final String base, final String include) {
    final Set<String> candidates = new HashSet<String>();

    final int starstar = include.indexOf("**");
    if (starstar > -1) {
      candidates.addAll(filterRecursive(base, include, starstar, starstar > 0));
    } else {
      final int star = include.indexOf('*');
      if (star > -1) {
        candidates.addAll(filterRecursive(base, include, star, true));
      } else {
        candidates.add(findDirect(base, include));
      }
    }

    return candidates;
  }

  @SuppressWarnings("unchecked")
  private Set<String> findRecursive(final String base) {
    final Set<String> matches = new HashSet<String>();

    for (final String match : (Set<String>) this.servletContext
        .getResourcePaths(base)) {
      if (match.endsWith("/")) {
        matches.addAll(findRecursive(match));
      } else {
        matches.add(match);
      }
    }

    return matches;
  }

  private Set<String> filterRecursive(final String base, final String include,
      final int index, final boolean prefixPatternWithBase) {
    return filterRecursive(findRecursive(base + include.substring(0, index)),
        base, include, prefixPatternWithBase);
  }

  private Set<String> filterRecursive(final Set<String> matches,
      final String base, final String include,
      final boolean prefixPatternWithBase) {
    final Set<String> result = new HashSet<String>();

    String pattern = include.replace(".", "\\.").replace("**", "#starstar#")
        .replace("*", "[^/]+").replace("#starstar#", ".*");
    if (prefixPatternWithBase) {
      pattern = base + pattern;
    }
    final Pattern expr = Pattern.compile(pattern);
    for (final String match : matches) {
      if (match != null && expr.matcher(match).matches()) {
        result.add(match);
      }
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  private String findDirect(final String base, final String include) {
    final String parent = include.substring(0, include.lastIndexOf('/'));
    for (final String match : (Set<String>) this.servletContext
        .getResourcePaths(base + parent)) {
      if (match.equals(base + include)) {
        return match;
      }
    }
    return null;
  }

}
