package com.sinnerschrader.smaller.closure;

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
import com.sinnerschrader.smaller.resource.Processor;
import com.sinnerschrader.smaller.resource.Resource;
import com.sinnerschrader.smaller.resource.StringResource;
import com.sinnerschrader.smaller.resource.Type;

/**
 * @author marwol
 */
public class ClosureProcessor implements Processor {

  /**
   * @see com.sinnerschrader.smaller.resource.Processor#supportsType(com.sinnerschrader.smaller.resource.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.JS;
  }

  /**
   * @see com.sinnerschrader.smaller.resource.Processor#execute(com.sinnerschrader.smaller.resource.Resource)
   */
  @Override
  public Resource execute(final Resource resource) throws IOException {
    final StringWriter writer = new StringWriter();
    compile(new StringReader(resource.getContents()), writer);
    return new StringResource(resource.getResolver(), resource.getType(),
        resource.getPath(), writer.toString());
  }

  private void compile(final Reader reader, final Writer writer)
      throws IOException {
    Compiler.setLoggingLevel(Level.SEVERE);
    final Compiler compiler = new Compiler();
    final CompilerOptions compilerOptions = new CompilerOptions();
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
