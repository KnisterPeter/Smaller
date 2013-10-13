package de.matrixweb.smaller.closure;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.javascript.jscomp.ClosureCodingConvention;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;

import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.StringResource;
import de.matrixweb.smaller.resource.Type;
import de.matrixweb.smaller.resource.vfs.VFS;

/**
 * @author marwol
 */
public class ClosureProcessor implements Processor {

  private static final LoggerOutputStream LOGGER_OUTPUT_STREAM = new LoggerOutputStream();

  /**
   * @see de.matrixweb.smaller.resource.Processor#supportsType(de.matrixweb.smaller.resource.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.JS;
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.smaller.resource.vfs.VFS,
   *      de.matrixweb.smaller.resource.Resource, java.util.Map)
   */
  @Override
  public Resource execute(final VFS vfs, final Resource resource,
      final Map<String, String> options) throws IOException {
    final StringWriter writer = new StringWriter();
    compile(new StringReader(resource.getContents()), writer);
    return new StringResource(resource.getResolver(), resource.getType(),
        resource.getPath(), writer.toString());
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#dispose()
   */
  @Override
  public void dispose() {
  }

  private void compile(final Reader reader, final Writer writer)
      throws IOException {
    Compiler.setLoggingLevel(Level.SEVERE);
    final Compiler compiler = new Compiler(
        new PrintStream(LOGGER_OUTPUT_STREAM));
    final CompilerOptions compilerOptions = new CompilerOptions();
    compilerOptions.setCodingConvention(new ClosureCodingConvention());
    CompilationLevel.SIMPLE_OPTIMIZATIONS
        .setOptionsForCompilationLevel(compilerOptions);
    compiler.initOptions(compilerOptions);

    final Result result = compiler.compile(SourceFile.fromCode("externs", ""),
        SourceFile.fromReader("source.js", reader), compilerOptions);
    if (result.success) {
      writer.write(compiler.toSource());
    } else {
      if (result.errors.length > 0) {
        throw new SmallerException("Closure Failed: "
            + result.errors[0].toString());
      }
    }
  }

  private static class LoggerOutputStream extends OutputStream {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(ClosureProcessor.class);

    private final StringBuilder sb = new StringBuilder();

    /**
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write(final int b) throws IOException {
      this.sb.append((char) b);
      if ((char) b == '\n') {
        print();
      }
    }

    /**
     * @see java.io.OutputStream#flush()
     */
    @Override
    public void flush() throws IOException {
      print();
      super.flush();
    }

    /**
     * @see java.io.OutputStream#close()
     */
    @Override
    public void close() throws IOException {
      print();
      super.close();
    }

    private void print() {
      if (this.sb.length() > 0) {
        LOGGER.info(this.sb.toString());
        this.sb.setLength(0);
      }
    }

  }

}
