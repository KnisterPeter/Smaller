package de.matrixweb.smaller.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.matrixweb.smaller.config.ConfigFile;
import de.matrixweb.smaller.config.Environment;
import de.matrixweb.smaller.config.Processor;

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
   * @param configFile
   * @return Returns the created {@link Manifest}
   */
  public static Manifest fromConfigFile(final ConfigFile configFile) {
    final Manifest manifest = new Manifest();
    for (final Environment env : configFile.getEnvironments().values()) {
      final ProcessDescription processDescription = new ProcessDescription();
      if (env.getPipeline() != null) {
        processDescription.setInputFile(env.getProcessors()
            .get(env.getPipeline()[0]).getSrc());
      }
      if (env.getProcess() != null) {
        processDescription.setOutputFile(env.getProcess());
      }
      if (env.getPipeline() != null) {
        for (final String name : env.getPipeline()) {
          final de.matrixweb.smaller.common.ProcessDescription.Processor processor = new de.matrixweb.smaller.common.ProcessDescription.Processor();
          processor.setName(name);
          final Processor p = env.getProcessors().get(name);
          if (p != null) {
            processor.getOptions().putAll(p.getPlainOptions());
          }
          processDescription.getProcessors().add(processor);
        }
      }

      manifest.getProcessDescriptions().add(processDescription);
    }
    manifest.getOptions().put("output:out-only",
        configFile.getBuildServer().isOutputOnly());
    return manifest;
  }

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
