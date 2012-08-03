package com.sinnerschrader.smaller.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @author marwol
 */
public class Manifest {

  private Task[] tasks;

  @JsonIgnore
  private int current = -1;

  /**
   * 
   */
  public Manifest() {
  }

  /**
   * @param tasks
   */
  public Manifest(final Task[] tasks) {
    this.tasks = tasks;
  }

  /**
   * @param task
   */
  public Manifest(final Task task) {
    this.tasks = new Task[] { task };
  }

  /**
   * @return the tasks
   */
  public final Task[] getTasks() {
    return this.tasks;
  }

  /**
   * @param tasks
   *          the tasks to set
   */
  public final void setTasks(final Task[] tasks) {
    this.tasks = tasks;
  }

  /**
   * @return the current task
   */
  public final Task getCurrent() {
    return this.tasks[this.current];
  }

  /**
   * @return the next task
   */
  @JsonIgnore
  public final Task getNext() {
    this.current++;
    if (this.tasks.length == this.current) {
      return null;
    }
    return this.tasks[this.current];
  }

  /** */
  public static class Task {

    /** */
    public static enum Options {
      /** */
      OUT_ONLY
    }

    private String processor;

    private String[] in;

    private String[] out;

    private Set<Options> options;

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
      this.in = in;
      this.out = out;
    }

    /**
     * @param processor
     * @param in
     * @param out
     */
    public Task(final String processor, final String in, final String out) {
      this.processor = processor;
      this.in = in.split(",");
      this.out = out.split(",");
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
      return this.in;
    }

    /**
     * @param extensions
     *          The extensions to filter for
     * @return Returns a list of input files filtered by given extensions
     */
    public final String[] getIn(final String... extensions) {
      List<String> list = new ArrayList<String>();
      for (String s : getIn()) {
        for (String ext : extensions) {
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
      this.in = in;
    }

    /**
     * @return the out
     */
    public final String[] getOut() {
      return this.out;
    }

    /**
     * @param out
     *          the out to set
     */
    public final void setOut(final String[] out) {
      this.out = out;
    }

    /**
     * @return the options
     */
    public final Set<Options> getOptions() {
      return this.options;
    }

    /**
     * @param options
     *          the options to set
     */
    public final void setOptions(final Set<Options> options) {
      this.options = options;
    }

  }

}
