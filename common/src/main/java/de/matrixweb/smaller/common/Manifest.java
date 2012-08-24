package de.matrixweb.smaller.common;


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

}
