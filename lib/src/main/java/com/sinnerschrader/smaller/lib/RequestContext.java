package com.sinnerschrader.smaller.lib;

import java.io.File;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.sinnerschrader.smaller.common.Manifest;

/**
 * @author marwol
 */
public class RequestContext {

  private File inputZip;

  private File input;

  private Manifest manifest;

  private File output;

  private ByteArrayOutputStream outputZip;

  /**
   * @return the inputZip
   */
  public final File getInputZip() {
    return inputZip;
  }

  /**
   * @param inputZip
   *          the inputZip to set
   */
  public final void setInputZip(final File inputZip) {
    this.inputZip = inputZip;
  }

  /**
   * @return the input
   */
  public final File getInput() {
    return input;
  }

  /**
   * @param input
   *          the input to set
   */
  public final void setInput(final File input) {
    this.input = input;
  }

  /**
   * @return the manifest
   */
  public final Manifest getManifest() {
    return manifest;
  }

  /**
   * @param manifest
   *          the manifest to set
   */
  public final void setManifest(final Manifest manifest) {
    this.manifest = manifest;
  }

  /**
   * @return the output
   */
  public final File getOutput() {
    return output;
  }

  /**
   * @param output
   *          the output to set
   */
  public final void setOutput(final File output) {
    this.output = output;
  }

  /**
   * @return the outputZip
   */
  public final ByteArrayOutputStream getOutputZip() {
    return outputZip;
  }

  /**
   * @param outputZip
   *          the outputZip to set
   */
  public final void setOutputZip(final ByteArrayOutputStream outputZip) {
    this.outputZip = outputZip;
  }

}
