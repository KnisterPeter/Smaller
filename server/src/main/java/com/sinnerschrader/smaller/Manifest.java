package com.sinnerschrader.smaller;

import java.io.File;
import java.io.IOException;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;

import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;

/**
 * @author marwol
 */
public class Manifest {

  private Task[] tasks;

  @JsonIgnore
  private int current = -1;

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
  public final void setTasks(Task[] tasks) {
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
  public final Task getNext() {
    this.current++;
    if (this.tasks.length == this.current) {
      return null;
    }
    return this.tasks[this.current];
  }

  /** */
  public static class Task {

    private String processor;

    private String[] in;

    private String[] out;

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
    public final void setProcessor(String processor) {
      this.processor = processor;
    }

    /**
     * @return the in
     */
    public final String[] getIn() {
      return this.in;
    }

    /**
     * @param in
     *          the in to set
     */
    public final void setIn(String[] in) {
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
    public final void setOut(String[] out) {
      this.out = out;
    }

    /**
     * @param base
     * @return a wro model with one group 'all' and all input parameters
     * @throws IOException
     */
    public WroModelFactory getWroModelFactory(final File base) throws IOException {
      ObjectMapper om = new ObjectMapper();
      final String[] files = om.readValue(new File(base, this.in[0]), String[].class);
      return new WroModelFactory() {

        public WroModel create() {
          Group group = new Group("all");
          for (String in : files) {
            group.addResource(Resource.create(new File(base, in).toURI().toString()));
          }
          return new WroModel().addGroup(group);
        }

        public void destroy() {
        }
      };
    }

  }

}
