package de.matrixweb.smaller.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @deprecated Replaced by {@link ProcessDescription}
 */
@Deprecated
public class Task {

  /**
   * @deprecated
   */
  public static enum Options {
    /** */
    OUT_ONLY
  }

  private String processor;

  private String[] in;

  private String[] out;

  private String optionsDefinition;

  private transient Map<String, Map<String, Object>> parsedOptions;

  /**
   * 
   */
  public Task() {
  }

  /**
   * @param processor
   * @param in
   * @param out
   */
  public Task(final String processor, final String[] in, final String[] out) {
    this.processor = processor;
    setIn(in);
    setOut(out);
  }

  /**
   * @param processor
   * @param in
   * @param out
   */
  public Task(final String processor, final String in, final String out) {
    this.processor = processor;
    this.in = in != null ? in.split(",") : new String[0];
    this.out = out != null ? out.split(",") : new String[0];
  }

  /**
   * @param processor
   * @param in
   * @param out
   * @param options
   * @deprecated Replaced by {@link Task#Task(String, String, String, String)}
   */
  @Deprecated
  public Task(final String processor, final String in, final String out,
      final Set<Task.Options> options) {
    this(processor, in, out);
    setOptions(options);
  }

  /**
   * @param processor
   * @param in
   * @param out
   * @param optionsDefinition
   */
  public Task(final String processor, final String in, final String out,
      final String optionsDefinition) {
    this(processor, in, out);
    setOptionsDefinition(optionsDefinition);
  }

  /**
   * @return the processor
   */
  public final String getProcessor() {
    return this.processor;
  }

  /**
   * @param processor
   *          the processor to set
   */
  public final void setProcessor(final String processor) {
    this.processor = processor;
  }

  /**
   * @return the in
   */
  public final String[] getIn() {
    final String[] result = new String[this.in.length];
    System.arraycopy(this.in, 0, result, 0, this.in.length);
    return result;
  }

  /**
   * @param extensions
   *          The extensions to filter for
   * @return Returns a list of input files filtered by given extensions
   */
  public final String[] getIn(final String... extensions) {
    final List<String> list = new ArrayList<String>();
    for (final String s : getIn()) {
      for (final String ext : extensions) {
        if (s.endsWith(ext)) {
          list.add(s);
        }
      }
    }
    return list.toArray(new String[list.size()]);
  }

  /**
   * @param in
   *          the in to set
   */
  public final void setIn(final String[] in) {
    this.in = new String[in.length];
    System.arraycopy(in, 0, this.in, 0, in.length);
  }

  /**
   * @return the out
   */
  public final String[] getOut() {
    final String[] result = new String[this.out.length];
    System.arraycopy(this.out, 0, result, 0, this.out.length);
    return result;
  }

  /**
   * @param out
   *          the out to set
   */
  public final void setOut(final String[] out) {
    this.out = new String[out.length];
    System.arraycopy(out, 0, this.out, 0, out.length);
  }

  /**
   * @return Returns null in any case
   * @deprecated Replaced by the more flexible {@link #getOptionsDefinition()}
   */
  @Deprecated
  public final Set<Task.Options> getOptions() {
    return null;
  }

  /**
   * @param options
   *          the optionsDefinition to set
   * @deprecated Replaced by the more flexible
   *             {@link #setOptionsDefinition(String)}
   */
  @Deprecated
  public final void setOptions(final Set<Task.Options> options) {
    if (options != null && options.contains(Options.OUT_ONLY)) {
      setOptionsDefinition("output:out-only=true");
    }
  }

  /**
   * @return the optionsDefinition
   */
  public final String getOptionsDefinition() {
    return this.optionsDefinition;
  }

  /**
   * @param processor
   *          The processor name to get the task options for
   * @return Returns a map with options
   */
  public Map<String, Object> getOptionsFor(final String processor) {
    if (this.parsedOptions == null) {
      this.parsedOptions = new HashMap<String, Map<String, Object>>();
      if (this.optionsDefinition != null) {
        for (final String byProcessor : this.optionsDefinition.split(";")) {
          StringTokenizer tokenizer = new StringTokenizer(byProcessor, ":");
          final String name = tokenizer.nextToken();
          this.parsedOptions.put(name, new HashMap<String, Object>());
          for (final String option : tokenizer.nextToken().split(",")) {
            tokenizer = new StringTokenizer(option, "=");
            this.parsedOptions.get(name).put(tokenizer.nextToken(),
                tokenizer.nextToken());
          }
        }
      }
    }
    final Map<String, Object> options = getProcessOrOptions(processor);
    if (!options.containsKey("source-maps")) {
      options.put("source-maps", Boolean.toString(GlobalOptions
          .isGenerateSourceMaps(getProcessOrOptions("global")
              .get("source-maps"))));
    }
    return options;
  }

  private Map<String, Object> getProcessOrOptions(final String name) {
    Map<String, Object> options = this.parsedOptions.get(name);
    if (options == null) {
      options = new HashMap<String, Object>();
    }
    return options;
  }

  /**
   * @param optionsDefinition
   *          the optionsDefinition to set
   */
  public final void setOptionsDefinition(final String optionsDefinition) {
    this.optionsDefinition = optionsDefinition;
  }

}
