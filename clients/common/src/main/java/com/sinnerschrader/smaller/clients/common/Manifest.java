package com.sinnerschrader.smaller.clients.common;

import java.util.ArrayList;
import java.util.List;

/**
 * @author marwol
 */
public class Manifest {

  private List<Task> tasks = new ArrayList<Task>();

  /**
   * @param task
   */
  public Manifest(Task task) {
    if (task != null) {
      getTasks().add(task);
    }
  }

  /**
   * @return the tasks
   */
  public final List<Task> getTasks() {
    return this.tasks;
  }

}
