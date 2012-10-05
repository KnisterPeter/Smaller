package de.matrixweb.smaller.pipeline;

import java.util.List;

import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.Resources;
import de.matrixweb.smaller.resource.Type;

/**
 * @author marwol
 */
public class Result {

  private Resources resources;

  /**
   * 
   */
  public Result() {
  }

  /**
   * @param resources
   */
  public Result(final Resources resources) {
    this.resources = resources;
  }

  /**
   * @param type
   * @return Returns the {@link Resource} matching the given type or null if
   *         none available
   */
  public Resource get(final Type type) {
    if (this.resources == null) {
      return null;
    }
    final List<Resource> res = this.resources.getByType(type);
    return res.size() > 0 ? res.get(0) : null;
  }

  /**
   * @return the js
   * @deprecated Use {@link #get(Type)} instead
   */
  @Deprecated
  public final Resource getJs() {
    return get(Type.JS);
  }

  /**
   * @return the css
   * @deprecated Use {@link #get(Type)} instead
   */
  @Deprecated
  public final Resource getCss() {
    return get(Type.CSS);
  }

}
