package com.sinnerschrader.smaller.clients.common;

/**
 * @author marwol
 */
public class Task {

  private String processor;

  private String[] in;

  private String[] out;

  /**
   * @param processor
   * @param in
   * @param out
   */
  public Task(String processor, String in, String out) {
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

}
