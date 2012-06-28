package com.sinnerschrader.smaller.closure;

import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;

import com.google.javascript.jscomp.CompilerOptions;

/**
 * @author marwol
 */
public class ClosureCompressorProcessor extends GoogleClosureCompressorProcessor {

  /**
   * @see ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor#newCompilerOptions()
   */
  @Override
  protected CompilerOptions newCompilerOptions() {
    return new CompilerOptions();
  }

}
