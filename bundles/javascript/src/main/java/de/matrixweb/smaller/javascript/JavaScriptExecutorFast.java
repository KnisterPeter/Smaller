package de.matrixweb.smaller.javascript;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author markusw
 */
public class JavaScriptExecutorFast implements JavaScriptExecutor {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(JavaScriptExecutorFast.class);

  private JavaScriptExecutor executor;

  /**
   * @param name
   * @param optimizationLevel
   * @param clazz
   */
  public JavaScriptExecutorFast(final String name, final int optimizationLevel,
      final Class<?> clazz) {
    try {
      LOGGER.info("Try v8 executor");
      this.executor = new JavaScriptExecutorV8(name, clazz);
    } catch (final NoClassDefFoundError e) {
      LOGGER.info("Fallback to rhino executor");
      this.executor = new JavaScriptExecutorRhino(name, optimizationLevel,
          clazz);
    } catch (final UnsatisfiedLinkError e) {
      LOGGER.info("Fallback to rhino executor");
      this.executor = new JavaScriptExecutorRhino(name, optimizationLevel,
          clazz);
    }
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#addGlobalFunction(java.lang.String,
   *      java.lang.Object)
   */
  @Override
  public void addGlobalFunction(final String name, final Object object) {
    this.executor.addGlobalFunction(name, object);
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#addGlobalFunction(java.lang.String,
   *      java.lang.Object, java.lang.String)
   */
  @Override
  public void addGlobalFunction(final String name, final Object object,
      final String method) {
    this.executor.addGlobalFunction(name, object, method);
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#addScriptSource(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void addScriptSource(final String source, final String name) {
    this.executor.addScriptSource(source, name);
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#addScriptFile(java.lang.String)
   */
  @Override
  public void addScriptFile(final String file) {
    this.executor.addScriptFile(file);
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#addScriptFile(java.net.URL)
   */
  @Override
  public void addScriptFile(final URL url) {
    this.executor.addScriptFile(url);
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#addCallScript(java.lang.String)
   */
  @Override
  public void addCallScript(final String source) {
    this.executor.addCallScript(source);
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#run(java.io.Reader,
   *      java.io.Writer)
   */
  @Override
  public void run(final Reader input, final Writer output) throws IOException {
    this.executor.run(input, output);
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#dispose()
   */
  @Override
  public void dispose() {
    this.executor.dispose();
  }

}
