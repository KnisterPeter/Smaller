package de.matrixweb.smaller.javascript;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;

import de.matrixweb.ne.NativeEngine;
import de.matrixweb.ne.StringFunctor;
import de.matrixweb.smaller.common.SmallerException;

/**
 * @author marwol
 */
public class JavaScriptExecutorV8 implements JavaScriptExecutor {

  private final NativeEngine engine;

  private String source;

  /**
   * @param name
   */
  public JavaScriptExecutorV8(final String name) {
    this(name, JavaScriptExecutorV8.class);
  }

  /**
   * @param name
   * @param clazz
   */
  public JavaScriptExecutorV8(final String name, final Class<?> clazz) {
    this.engine = new NativeEngine(new StringFunctor("") {
      @Override
      public String call(final String require) {
        final String module = "/" + name + "/" + require + ".js";
        try {
          final InputStream in = clazz.getResourceAsStream(module);
          if (in == null) {
            throw new SmallerException("Failed to find required module: "
                + module);
          }
          try {
            return IOUtils.toString(in);
          } finally {
            in.close();
          }
        } catch (final IOException e) {
          throw new SmallerException("Failed to find required module: "
              + module, e);
        }
      }
    });
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
      final String method) {
    try {
      final Method javaMethod = object.getClass().getMethod(method,
          String.class);
      this.engine.addCallbackFunction(new StringFunctor(name) {
        @Override
        public String call(final String input) {
          try {
            final String res = javaMethod.invoke(object, input).toString();
            return res;
          } catch (final IllegalAccessException e) {
            throw new SmallerException("Illegal access to callback method", e);
          } catch (final InvocationTargetException e) {
            throw new SmallerException("Failed to execute callback method", e
                .getTargetException());
          }
        }
      });
    } catch (final NoSuchMethodException e) {
      throw new SmallerException("Failed to reflect global method", e);
    }
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#addScriptSource(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void addScriptSource(final String source, final String name) {
    this.engine.addScript(name, source);
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#addScriptFile(java.lang.String)
   */
  @Override
  public void addScriptFile(final String file) {
    final InputStream script = getClass().getResourceAsStream(file);
    try {
      addScriptSource(IOUtils.toString(script), file);
    } catch (final IOException e) {
      throw new SmallerException("Failed to include script file", e);
    } finally {
      if (script != null) {
        IOUtils.closeQuietly(script);
      }
    }
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#addScriptFile(java.net.URL)
   */
  @Override
  public void addScriptFile(final URL url) {
    try {
      addScriptSource(IOUtils.toString(url), url.getFile());
    } catch (final IOException e) {
      throw new SmallerException("Failed to include script file", e);
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
    final String data = new ObjectMapper().writeValueAsString(IOUtils
        .toString(input));
    output.write(this.engine.execute(String.format(this.source, data)));
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#dispose()
   */
  @Override
  public void dispose() {
    this.engine.dispose();
  }

}
