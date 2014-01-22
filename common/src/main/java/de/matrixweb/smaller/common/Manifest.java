package de.matrixweb.smaller.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @author marwol
 */
public class Manifest {

  private List<ProcessDescription> processDescriptions;

  private final Map<String, Object> options = new HashMap<String, Object>();

  @Deprecated
  private Task[] tasks;

  @Deprecated
  @JsonIgnore
  private int current = -1;

  /**
   * 
   */
  public Manifest() {
  }

  /**
   * @return the processDescriptions
   */
  public List<ProcessDescription> getProcessDescriptions() {
    if (this.processDescriptions == null) {
      this.processDescriptions = new ArrayList<ProcessDescription>();
    }
    return this.processDescriptions;
  }

  /**
   * @return the options
   */
  public Map<String, Object> getOptions() {
    return this.options;
  }

  /**
   * @param tasks
   */
  @Deprecated
  public Manifest(final Task[] tasks) {
    this.tasks = tasks;
  }

  /**
   * @param task
   */
  @Deprecated
  public Manifest(final Task task) {
    this.tasks = new Task[] { task };
  }

  /**
   * @return the tasks
   */
  @Deprecated
  public final Task[] getTasks() {
    if (this.tasks == null) {
      return new Task[0];
    }
    return this.tasks;
  }

  /**
   * @param tasks
   *          the tasks to set
   */
  @Deprecated
  public final void setTasks(final Task[] tasks) {
    this.tasks = tasks;
  }

  /**
   * @return the current task
   */
  @Deprecated
  public final Task getCurrent() {
    return this.tasks[this.current];
  }

  /**
   * @return the next task
   */
  @JsonIgnore
  @Deprecated
  public final Task getNext() {
    this.current++;
    if (this.tasks.length == this.current) {
      return null;
    }
    return this.tasks[this.current];
  }

}
