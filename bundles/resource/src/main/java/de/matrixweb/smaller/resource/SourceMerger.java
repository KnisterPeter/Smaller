package de.matrixweb.smaller.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author marwol
 */
public class SourceMerger {

  private final Boolean uniqueFiles;

  public SourceMerger() {
    this.uniqueFiles = Boolean.FALSE;
  }

  /**
   * @param uniqueFiles
   *          flag to resolve a json source files just once
   */
  public SourceMerger(final Boolean uniqueFiles) {
    this.uniqueFiles = uniqueFiles;
  }

  /**
   * @param resolver
   *          The {@link ResourceResolver} to lookup sources
   * @param in
   *          The list of input files
   * @return Returns the merged result
   * @throws IOException
   */
  public String merge(final ResourceResolver resolver, final List<String> in)
      throws IOException {
    return merge(getResources(resolver, in));
  }

  /**
   * @param resolver
   *          The {@link ResourceResolver} to lookup sources
   * @param in
   *          The list of input files
   * @return Returns a {@link List} of resolved {@link Resource}s
   * @throws IOException
   */
  public List<Resource> getResources(final ResourceResolver resolver,
      final List<String> in) throws IOException {
    return getSourceFiles(resolver, in);
  }

  /**
   * @param resources
   *          The {@link Resource}s to merge
   * @return Returns the merged resources
   * @throws IOException
   */
  public String merge(final List<Resource> resources) throws IOException {
    return this.merge(resources, "\n");
  }

  private String merge(final List<Resource> resources, final String separator)
      throws IOException {
    final List<String> contents = new ArrayList<String>();
    for (final Resource resource : resources) {
      contents.add(resource.getContents());
    }
    return StringUtils.join(contents, separator);
  }

  private List<Resource> getSourceFiles(final ResourceResolver resolver,
      final List<String> in) throws IOException {
    final List<Resource> inputs = new ArrayList<Resource>();
    for (final String s : in) {
      final Resource resource = resolver.resolve(s);
      if (resource.getPath().endsWith("json")) {
        inputs.addAll(getJsonSourceFiles(resource.getResolver(), resource));
      } else {
        inputs.add(resource);
      }
    }
    return inputs;
  }

  private List<Resource> getJsonSourceFiles(final ResourceResolver resolver,
      final Resource resource) throws IOException {
    final List<Resource> list = new ArrayList<Resource>();
    final Set<String> alreadyHandled = new HashSet<String>();
    for (final String s : new ObjectMapper().readValue(resource.getContents(),
        String[].class)) {
      if (!isUniqueFileResolved(alreadyHandled, s)) {
        list.add(resolver.resolve(s));
        alreadyHandled.add(s);
      }
    }
    return list;
  }

  /**
   * Examines a source file if it is already resolved when it should be unique.
   * 
   * @param alreadyHandled
   *          all source files already resolved
   * @param s
   *          source file to resolve
   * @return
   *        true if source files should be unique and the source file was not resolved yet
   */
  private boolean isUniqueFileResolved(final Set<String> alreadyHandled,
      final String s) {
    return this.uniqueFiles && alreadyHandled.contains(s);
  }

}
