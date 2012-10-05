package de.matrixweb.smaller.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implements a resource group.
 * 
 * @author marwol
 */
public class Resources {

  private final List<Resource> resources = new ArrayList<Resource>();

  /**
   * 
   */
  public Resources() {
  }

  /**
   * @param resources
   */
  public Resources(final List<Resource> resources) {
    this.resources.addAll(resources);
  }

  /**
   * @param resources
   */
  public Resources(final Resource... resources) {
    this(Arrays.asList(resources));
  }

  /**
   * @return the resources
   */
  public final List<Resource> getResources() {
    return this.resources;
  }

  /**
   * @param resources
   *          the resources to set
   */
  public final void setResources(final List<Resource> resources) {
    this.resources.addAll(resources);
  }

  /**
   * @param resource
   *          the resource to add
   */
  public final void addResource(final Resource resource) {
    this.resources.add(resource);
  }

  /**
   * @param type
   * @return Returns a list of {@link Resource}s matching the given type
   */
  public final List<Resource> getByType(final Type type) {
    final List<Resource> list = new ArrayList<Resource>();
    for (final Resource resource : this.resources) {
      if (resource.getType() == type) {
        list.add(resource);
      }
    }
    return list;
  }

  /**
   * Replaces all <code>r1</code> {@link Resource}s with <code>r2</code>
   * {@link Resource}s.
   * 
   * @param r1
   * @param r2
   */
  public final void replace(final List<Resource> r1, final List<Resource> r2) {
    this.resources.removeAll(r1);
    this.resources.addAll(r2);
  }

  /**
   * Replaces all <code>r1</code> {@link Resource}s with <code>r2</code>
   * {@link Resource}s.
   * 
   * @param r1
   * @param r2
   */
  public final void replace(final List<Resource> r1, final Resource... r2) {
    replace(r1, Arrays.asList(r2));
  }

}
