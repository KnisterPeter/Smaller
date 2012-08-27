package de.matrixweb.smaller.clients.maven;


/**
 * @author markusw
 */
public class Task {

  /**
   * @parameter
   */
  private String processor;

  /**
   * @parameter
   */
  private String in;

  /**
   * @parameter
   */
  private String out;

  /**
   * The task options.
   * 
   * @parameter default-value=""
   */
  private String options;

  /**
   * @return the processor
   */
  public String getProcessor() {
    return this.processor;
  }

  /**
   * @param processor
   *          the processor to set
   */
  public void setProcessor(final String processor) {
    this.processor = processor;
  }

  /**
   * @return the in
   */
  public String getIn() {
    return this.in;
  }

  /**
   * @param in
   *          the in to set
   */
  public void setIn(final String in) {
    this.in = in;
  }

  /**
   * @return the out
   */
  public String getOut() {
    return this.out;
  }

  /**
   * @param out
   *          the out to set
   */
  public void setOut(final String out) {
    this.out = out;
  }

  /**
   * @return the options
   */
  public String getOptions() {
    return this.options;
  }

  /**
   * @param options
   *          the options to set
   */
  public void setOptions(final String options) {
    this.options = options;
  }

}
