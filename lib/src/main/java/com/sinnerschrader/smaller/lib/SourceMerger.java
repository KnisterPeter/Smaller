package com.sinnerschrader.smaller.lib;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.sinnerschrader.smaller.lib.resource.Resource;
import com.sinnerschrader.smaller.lib.resource.ResourceResolver;
import com.sinnerschrader.smaller.lib.resource.Type;

/**
 * @author marwol
 */
public class SourceMerger {

  /**
   * @param resolver The {@link ResourceResolver} to lookup sources
   * @param in The list of input files
   * @return Returns the merged result
   * @throws IOException
   */
  public String merge(ResourceResolver resolver, final List<String> in) throws IOException {
    return merge(getResources(resolver, in));
  }

  /**
   * @param resolver The {@link ResourceResolver} to lookup sources
   * @param in The list of input files
   * @return Returns a {@link List} of resolved {@link Resource}s
   * @throws IOException
   */
  public List<Resource> getResources(ResourceResolver resolver, List<String> in) throws IOException {
    return getSourceFiles(resolver, in);
  }
  
  /**
   * @param resources The {@link Resource}s to merge
   * @return Returns the merged resources
   * @throws IOException
   */
  public String merge(final List<Resource> resources) throws IOException {
    return this.merge(resources, "\n");
  }
  
  private String merge(final List<Resource> resources, final String separator) throws IOException {
    final List<String> contents = Lists.newArrayList();
    for (final Resource resource : resources) {
      contents.add(resource.getContents());
    }
    return Joiner.on(separator).join(contents);
  }

  private List<Resource> getSourceFiles(ResourceResolver resolver, final List<String> in) throws IOException {
    final List<Resource> inputs = Lists.newArrayList();
    for (final String s : in) {
      Resource resource = resolver.resolve(s);
      if (resource.getType() == Type.JSON) {
        inputs.addAll(this.getJsonSourceFiles(resolver, resource));
      } else {
        inputs.add(resource);
      }
    }
    return inputs;
  }

  private List<Resource> getJsonSourceFiles(ResourceResolver resolver, Resource resource) throws IOException {
    final List<Resource> list = Lists.newArrayList();
    for (final String s : new ObjectMapper().readValue(resource.getContents(), String[].class)) {
      list.add(resolver.resolve(s));
    }
    return list;
  }

}
