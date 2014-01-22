package de.matrixweb.smaller.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author markusw
 */
public class ProcessDescription {

  private final List<Processor> processors = new ArrayList<Processor>();

  private String inputFile;

  private String outputFile;

  /**
   * @return the processors
   */
  public List<Processor> getProcessors() {
    return this.processors;
  }

  /**
   * @return the inputFile
   */
  public String getInputFile() {
    return this.inputFile;
  }

  /**
   * @param inputFile
   *          the inputFile to set
   */
  public void setInputFile(final String inputFile) {
    this.inputFile = inputFile;
    if (inputFile != null && !this.inputFile.startsWith("/")) {
      this.inputFile = "/" + this.inputFile;
    }
  }

  /**
   * @return the outputFile
   */
  public String getOutputFile() {
    return this.outputFile;
  }

  /**
   * @param outputFile
   *          the outputFile to set
   */
  public void setOutputFile(final String outputFile) {
    this.outputFile = outputFile;
    if (this.outputFile != null && !this.outputFile.startsWith("/")) {
      this.outputFile = "/" + this.outputFile;
    }
  }

  /** */
  public static class Processor {

    private String name;

    private final Map<String, Object> options = new HashMap<String, Object>();

    /**
     * @return the name
     */
    public String getName() {
      return this.name;
    }

    /**
     * @param name
     *          the name to set
     */
    public void setName(final String name) {
      this.name = name;
    }

    /**
     * @return the options
     */
    public Map<String, Object> getOptions() {
      return this.options;
    }

  }

}
