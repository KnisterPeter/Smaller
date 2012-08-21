package com.sinnerschrader.smaller.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author marwol
 */
public class SourceMerger {

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
      if (resource.getType() == Type.JSON) {
        inputs.addAll(getJsonSourceFiles(resolver, resource));
      } else {
        inputs.add(resource);
      }
    }
    return inputs;
  }

  private List<Resource> getJsonSourceFiles(final ResourceResolver resolver,
      final Resource resource) throws IOException {
    final List<Resource> list = new ArrayList<Resource>();
    for (final String s : new ObjectMapper().readValue(resource.getContents(),
        String[].class)) {
      list.add(resolver.resolve(s));
    }
    return list;
  }

}
