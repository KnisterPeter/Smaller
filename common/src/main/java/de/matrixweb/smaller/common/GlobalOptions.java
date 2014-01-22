package de.matrixweb.smaller.common;

import org.apache.commons.lang3.BooleanUtils;

/**
 * Utility methods for global options.
 * 
 * @author markusw
 */
public class GlobalOptions {

  /**
   * @param value
   * @return Returns <code>true</code> if the <code>source-maps</code> option
   *         for the <code>global</code> processor is set to <code>true</code>
   *         or <code>yes</code>
   */
  static boolean isGenerateSourceMaps(final Object value) {
    return value == null ? false : BooleanUtils.toBoolean(value.toString());
  }

  /**
   * @param task
   * @return Returns <code>true</code> if the <code>out-only</code> option for
   *         the <code>output</code> processor is set to <code>true</code> or
   *         <code>yes</code>.
   * @deprecated
   */
  @Deprecated
  public static boolean isOutOnly(final Task task) {
    final Object value = task.getOptionsFor("output").get("out-only");
    return value == null ? false : BooleanUtils.toBoolean(value.toString());
  }

  /**
   * @param task
   * @return Returns <code>true</code> if the <code>once</code> option for the
   *         <code>source</code> processor is set to <code>true</code> or
   *         <code>yes</code>.
   * @deprecated
   */
  @Deprecated
  public static boolean isSourceOnce(final Task task) {
    final Object value = task.getOptionsFor("source").get("once");
    return value == null ? false : BooleanUtils.toBoolean(value.toString());
  }

  /**
   * @param manifest
   * @return Returns <code>true</code> if the <code>out-only</code> option for
   *         the <code>output</code> processor is set to <code>true</code> or
   *         <code>yes</code>.
   */
  public static boolean isOutOnly(final Manifest manifest) {
    final Object value = manifest.getOptions().get("output:out-only");
    return BooleanUtils.toBoolean(value != null ? value.toString() : "false");
  }

  /**
   * @param manifest
   * @return Returns <code>true</code> if the <code>once</code> option for the
   *         <code>source</code> processor is set to <code>true</code> or
   *         <code>yes</code>.
   */
  public static boolean isSourceOnce(final Manifest manifest) {
    final Object value = manifest.getOptions().get("source:once");
    return BooleanUtils.toBoolean(value != null ? value.toString() : "false");
  }

}
