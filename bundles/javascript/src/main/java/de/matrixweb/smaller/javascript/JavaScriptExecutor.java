package de.matrixweb.smaller.javascript;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;

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
public class JavaScriptExecutor {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(JavaScriptExecutor.class);

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
  public JavaScriptExecutor(final String name) {
    this(name, 9);
  }

  /**
   * This creates a new script environment.
   * 
   * @param name
   *          The path of the script to add
   * @param optimizationLevel
   *          The optimization level to use (-1 lowest, 9 highest)
   */
  public JavaScriptExecutor(final String name, final int optimizationLevel) {
    this.name = name;
    this.optimizationLevel = optimizationLevel;
    init();
  }

  private void init() {
    final Context context = Context.enter();
    context.setOptimizationLevel(this.optimizationLevel);
    context.setLanguageVersion(Context.VERSION_1_8);
    final ScriptableObject scope = context.initStandardObjects();
    final Require require = new Require(Context.getCurrentContext(), scope,
        getModuleScriptProvider(), null, null, false);
    require.install(scope);
    try {
      this.moduleScope = new ModuleScope(scope, new URI("./" + this.name), null);
    } catch (final URISyntaxException e) {
      throw new SmallerException("Failed to create moduleScope", e);
    }
    addProperty("logger", LOGGER);
  }

  /**
   * @param name
   *          The name to use of object access from scripts
   * @param object
   *          The object to make globally available in the environment
   */
  public final void addProperty(final String name, final Object object) {
    ScriptableObject.putProperty(this.moduleScope, name,
        Context.javaToJS(object, this.moduleScope));
  }

  /**
   * @param source
   *          The source code of a script to add
   * @param name
   *          The name of the source for debugging/error reporting
   */
  public void addScriptSource(final String source, final String name) {
    Context.getCurrentContext().evaluateString(this.moduleScope, source, name,
        1, null);
  }

  /**
   * @param file
   *          The path of the file to add
   */
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
   * @param is
   *          The {@link InputStream} containing the source
   * @param name
   *          The name of the source for debugging/error reporting
   * @throws IOException
   */
  public void addScriptFile(final InputStream is, final String name)
      throws IOException {
    Context.getCurrentContext().evaluateString(this.moduleScope,
        IOUtils.toString(is), name, 1, null);
  }

  /**
   * @param source
   *          The source script to evaluate. This should contain <code>%s</code>
   *          where the reader input should be placed.
   */
  public void addCallScript(final String source) {
    this.source = source;
  }

  /**
   * @param input
   *          The execution input parameters
   * @param output
   *          The execution result
   * @throws IOException
   */
  public void run(final Reader input, final Writer output) throws IOException {
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

  private ModuleScriptProvider getModuleScriptProvider() {
    return new ModuleScriptProvider() {
      @Override
      public ModuleScript getModuleScript(final Context cx,
          final String moduleId, final URI moduleUri, final URI baseUri,
          final Scriptable paths) throws IOException, URISyntaxException {
        return JavaScriptExecutor.this.getModuleScript(cx, moduleId);
      }
    };
  }

  private ModuleScript getModuleScript(final Context cx, final String moduleId)
      throws IOException, URISyntaxException {
    final String path = '/' + this.name + '/' + moduleId + ".js";
    final InputStream script = getClass().getResourceAsStream(path);
    if (script == null) {
      return null;
    }
    try {
      return new ModuleScript(cx.compileString(IOUtils.toString(script),
          moduleId, 1, null), new URI(moduleId), null);
    } finally {
      IOUtils.closeQuietly(script);
    }
  }

}
