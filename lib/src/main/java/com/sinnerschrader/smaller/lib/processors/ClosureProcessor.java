package com.sinnerschrader.smaller.lib.processors;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;

import com.google.javascript.jscomp.ClosureCodingConvention;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.sinnerschrader.smaller.lib.ProcessorChain.Type;
import com.sinnerschrader.smaller.lib.resource.Resource;
import com.sinnerschrader.smaller.lib.resource.StringResource;

/**
 * @author marwol
 */
public class ClosureProcessor implements Processor {

  /**
   * @see com.sinnerschrader.smaller.lib.processors.Processor#supportsType(com.sinnerschrader.smaller.lib.ProcessorChain.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.JS;
  }

  /**
   * @see com.sinnerschrader.smaller.lib.processors.Processor#execute(com.sinnerschrader.smaller.lib.resource.Resource)
   */
  @Override
  public Resource execute(final Resource resource) throws IOException {
    final StringWriter writer = new StringWriter();
    compile(new StringReader(resource.getContents()), writer);
    return new StringResource(resource.getResolver(), resource.getType(),
        resource.getPath(), writer.toString());
  }

  private void compile(Reader reader, Writer writer) throws IOException {
    Compiler.setLoggingLevel(Level.SEVERE);
    final Compiler compiler = new Compiler();
    CompilerOptions compilerOptions = new CompilerOptions();
    compilerOptions.setCodingConvention(new ClosureCodingConvention());
    CompilationLevel.SIMPLE_OPTIMIZATIONS
        .setOptionsForCompilationLevel(compilerOptions);
    compiler.initOptions(compilerOptions);

    final Result result = compiler.compile(SourceFile.fromCode("externs", ""),
        SourceFile.fromReader("source.js", reader), compilerOptions);
    if (result.success) {
      writer.write(compiler.toSource());
    }
  }

}
