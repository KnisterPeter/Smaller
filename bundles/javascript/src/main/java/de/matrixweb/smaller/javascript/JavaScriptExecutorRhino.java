package de.matrixweb.smaller.javascript;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.commonjs.module.ModuleScope;
import org.mozilla.javascript.commonjs.module.ModuleScript;
import org.mozilla.javascript.commonjs.module.ModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.Require;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.matrixweb.smaller.common.SmallerException;

/**
 * @author markusw
 */
public class JavaScriptExecutorRhino implements JavaScriptExecutor {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(JavaScriptExecutorRhino.class);

  private boolean initializing = true;

  private final String name;

  private final int optimizationLevel;

  private ModuleScope moduleScope;

  private String source;

  /**
   * This creates a new script environment with the optimization level set to
   * maximum.
   * 
   * @param name
   *          The path of the script to add
   */
  public JavaScriptExecutorRhino(final String name) {
    this(name, 9, JavaScriptException.class);
  }

  /**
   * This creates a new script environment with the optimization level set to
   * maximum.
   * 
   * @param name
   *          The path of the script to add
   * @param clazz
   *          The class used for resource resolving
   */
  public JavaScriptExecutorRhino(final String name, final Class<?> clazz) {
    this(name, 9, clazz);
  }

  /**
   * This creates a new script environment.
   * 
   * @param name
   *          The path of the script to add
   * @param optimizationLevel
   *          The optimization level to use (-1 lowest, 9 highest)
   */
  public JavaScriptExecutorRhino(final String name, final int optimizationLevel) {
    this(name, optimizationLevel, JavaScriptException.class);
  }

  /**
   * This creates a new script environment.
   * 
   * @param name
   *          The path of the script to add
   * @param optimizationLevel
   *          The optimization level to use (-1 lowest, 9 highest)
   * @param clazz
   *          The class used for resource resolving
   */
  public JavaScriptExecutorRhino(final String name,
      final int optimizationLevel, final Class<?> clazz) {
    this.name = name;
    this.optimizationLevel = optimizationLevel;
    init(clazz);
  }

  private void init(final Class<?> clazz) {
    final Context context = Context.enter();
    context.setOptimizationLevel(this.optimizationLevel);
    context.setLanguageVersion(Context.VERSION_1_8);
    final ScriptableObject scope = context.initStandardObjects();
    final Require require = new Require(Context.getCurrentContext(), scope,
        getModuleScriptProvider(clazz), null, null, false);
    require.install(scope);
    try {
      this.moduleScope = new ModuleScope(scope, new URI("./" + this.name), null);
    } catch (final URISyntaxException e) {
      throw new SmallerException("Failed to create moduleScope", e);
    }
    addGlobalFunction("print", LOGGER, "info");
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#addGlobalFunction(java.lang.String,
   *      java.lang.Object)
   */
  @Override
  public final void addGlobalFunction(final String name, final Object object) {
    addGlobalFunction(name, object, name);
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#addGlobalFunction(java.lang.String,
   *      java.lang.Object, java.lang.String)
   */
  @Override
  public final void addGlobalFunction(final String name, final Object object,
      final String method) {
    // @formatter:off
    final String script = 
      "this['" + name + "'] = (function() {\n" +
      "    var fn = __" + name + "__['" + method + "'];\n" +
      "    return function() { return fn.apply(__" + name + "__, arguments); }\n" +
      "})();\n";
    // @formatter:on
    ScriptableObject.putProperty(this.moduleScope, "__" + name + "__",
        Context.javaToJS(object, this.moduleScope));
    addScriptSource(script, name + "_function_publisher");
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#addScriptSource(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void addScriptSource(final String source, final String name) {
    Context.getCurrentContext().evaluateString(this.moduleScope, source, name,
        1, null);
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#addScriptFile(java.lang.String)
   */
  @Override
  public void addScriptFile(final String file) {
    try {
      final InputStream script = getClass().getResourceAsStream(file);
      try {
        Context.getCurrentContext().evaluateString(this.moduleScope,
            IOUtils.toString(script), file, 1, null);
      } finally {
        IOUtils.closeQuietly(script);
      }
    } catch (final IOException e) {
      throw new SmallerException("Failed to include script file", e);
    }
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#addScriptFile(java.net.URL)
   */
  @Override
  public void addScriptFile(final URL url) {
    try {
      Context.getCurrentContext().evaluateString(this.moduleScope,
          IOUtils.toString(url), url.getFile(), 1, null);
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
    LOGGER.info("Executeing Rhino engine");
    if (this.initializing && Context.getCurrentContext() != null) {
      this.initializing = false;
      Context.exit();
    }
    final String data = new ObjectMapper().writeValueAsString(IOUtils
        .toString(input));

    final Context context = Context.enter();
    try {
      final ScriptableObject scope = (ScriptableObject) context
          .initStandardObjects(this.moduleScope);

      final Object result = context.evaluateString(scope,
          String.format(this.source, data), this.name, 1, null);

      output.append(String.valueOf(result));
    } catch (final JavaScriptException e) {
      throw new SmallerException("Failed to run javascript", e);
    } finally {
      Context.exit();
    }
  }

  /**
   * @see de.matrixweb.smaller.javascript.JavaScriptExecutor#dispose()
   */
  @Override
  public void dispose() {
  }

  private ModuleScriptProvider getModuleScriptProvider(final Class<?> clazz) {
    return new ModuleScriptProvider() {
      @Override
      public ModuleScript getModuleScript(final Context cx,
          final String moduleId, final URI moduleUri, final URI baseUri,
          final Scriptable paths) throws IOException, URISyntaxException {
        return JavaScriptExecutorRhino.this
            .getModuleScript(cx, moduleId, clazz);
      }
    };
  }

  private ModuleScript getModuleScript(final Context cx, final String moduleId,
      final Class<?> clazz) throws IOException, URISyntaxException {
    final String path = '/' + this.name + '/' + moduleId + ".js";
    final URL url = clazz.getResource(path);
    if (url == null) {
      return null;
    }
    return new ModuleScript(cx.compileString(IOUtils.toString(url), moduleId,
        1, null), new URI(moduleId), null);
  }

}
