package de.matrixweb.smaller.javascript;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.dynjs.Config;
import org.dynjs.exception.DynJSException;
import org.dynjs.runtime.DynJS;
import org.dynjs.runtime.Runner;
import org.dynjs.runtime.builtins.types.string.DynString;
import org.dynjs.runtime.modules.JavaFunction;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.matrixweb.smaller.common.SmallerException;

/**
 * @author markusw
 */
public class JavaScriptExecutorDynJs implements JavaScriptExecutor {

  private String source;

  private final DynJS runtime;

  private final Runner runner;

  private boolean prepared = false;

  /**
   * 
   */
  public JavaScriptExecutorDynJs() {
    final Config config = new Config();
    this.runtime = new DynJS(config);
    this.runner = this.runtime.newRunner();
    addScriptFile(getClass().getResource("/npm_modules.js"));
  }

  /**
   * @param classLoader
   */
  public JavaScriptExecutorDynJs(final ClassLoader classLoader) {
    final Config config = new Config(classLoader);
    this.runtime = new DynJS(config);
    this.runner = this.runtime.newRunner();
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#addGlobalFunction(java.lang.String,
   *      java.lang.Object)
   */
  @Override
  public void addGlobalFunction(final String name, final Object object) {
    addGlobalFunction(name, object, name);
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#addGlobalFunction(java.lang.String,
   *      java.lang.Object, java.lang.String)
   */
  @Override
  public void addGlobalFunction(final String name, final Object object,
      final String methodName) {
    Method method = null;
    for (final Method m : object.getClass().getMethods()) {
      if (m.getName().equals(methodName)) {
        method = m;
      }
    }
    if (method == null) {
      throw new SmallerException("Method '" + methodName + "' not found");
    }
    try {
      this.runtime
          .getExecutionContext()
          .getGlobalObject()
          .put(
              name,
              new JavaFunction(this.runtime.getExecutionContext()
                  .getGlobalObject(), object, method));
    } catch (final IllegalAccessException e) {
      throw new SmallerException("Failed to add method '" + methodName + "'", e);
    }
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#addScriptSource(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void addScriptSource(final String source, final String name) {
    if (!this.prepared) {
      this.runner.withSource(source).evaluate();
    }
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#addScriptFile(java.lang.String)
   */
  @Override
  public void addScriptFile(final String file) {
    if (!this.prepared) {
      this.runner.withFileName(file).evaluate();
    }
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#addScriptFile(java.net.URL)
   */
  @Override
  public void addScriptFile(final URL url) {
    if (!this.prepared) {
      try {
        this.runner.withSource(IOUtils.toString(url)).evaluate();
      } catch (final IOException e) {
        throw new SmallerException("Failed to read url", e);
      }
    }
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#addCallScript(java.lang.String)
   */
  @Override
  public void addCallScript(final String source) {
    this.source = source;
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#run(java.io.Reader,
   *      java.io.Writer)
   */
  @Override
  public void run(final Reader input, final Writer output) throws IOException {
    this.prepared = true;
    try {

      final String data = new ObjectMapper().writeValueAsString(IOUtils
          .toString(input));
      Object o = this.runner.withSource(String.format(this.source, data))
          .execute();
      if (o instanceof DynString) {
        o = ((DynString) o).getPrimitiveValue();
      }
      IOUtils.write(o.toString(), output);
    } catch (final DynJSException e) {
      throw new SmallerException("Failed to execute javascript", e);
    }
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#dispose()
   */
  @Override
  public void dispose() {
  }

}
