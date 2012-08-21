package com.sinnerschrader.smaller.javascript;

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

import com.sinnerschrader.smaller.common.SmallerException;

/**
 * @author markusw
 */
public class JavaScriptExecutor {

  private boolean initializing = true;

  private String name;

  private int optimizationLevel;

  private ModuleScope moduleScope;

  private String source;

  public JavaScriptExecutor(String name) {
    this(name, 9);
  }

  public JavaScriptExecutor(String name, int optimizationLevel) {
    this.name = name;
    this.optimizationLevel = optimizationLevel;
    init();
  }

  private final void init() {
    Context context = Context.enter();
    context.setOptimizationLevel(optimizationLevel);
    context.setLanguageVersion(Context.VERSION_1_8);
    ScriptableObject scope = context.initStandardObjects();
    Require require = new Require(Context.getCurrentContext(), scope,
        getModuleScriptProvider(), null, null, false);
    require.install(scope);
    try {
      moduleScope = new ModuleScope(scope, new URI("./" + name), null);
    } catch (URISyntaxException e) {
      throw new SmallerException("Failed to create moduleScope", e);
    }
  }

  public void addProperty(String name, Object object) {
    ScriptableObject.putProperty(moduleScope, name,
        Context.javaToJS(object, moduleScope));
  }

  public void addScriptSource(String source, String name) {
    Context.getCurrentContext().evaluateString(moduleScope, source, name, 1,
        null);
  }

  public void addScriptFile(String file) {
    try {
      InputStream script = getClass().getResourceAsStream(file);
      try {
        Context.getCurrentContext().evaluateString(moduleScope,
            IOUtils.toString(script), file, 1, null);
      } finally {
        IOUtils.closeQuietly(script);
      }

    } catch (IOException e) {
      throw new SmallerException("Failed to include script file", e);
    }
  }

  public void addScriptFile(String name, InputStream is) throws IOException {
    Context.getCurrentContext().evaluateString(moduleScope,
        IOUtils.toString(is), name, 1, null);
  }

  /**
   * @param source
   *          The source script to evaluate. This should contain <code>%s</code>
   *          where the reader input should be placed.
   */
  public void addCallScript(String source) {
    this.source = source;
  }

  public void run(Reader input, Writer output) throws IOException {
    if (initializing && Context.getCurrentContext() != null) {
      initializing = false;
      Context.exit();
    }
    String data = new ObjectMapper()
        .writeValueAsString(IOUtils.toString(input));

    Context context = Context.enter();
    try {
      ScriptableObject scope = (ScriptableObject) context
          .initStandardObjects(moduleScope);

      Object result = context.evaluateString(scope,
          String.format(source, data), name, 1, null);

      output.append(String.valueOf(result));
    } catch (JavaScriptException e) {
      throw new SmallerException("Failed to run javascript", e);
    } finally {
      Context.exit();
    }
  }

  private ModuleScriptProvider getModuleScriptProvider() {
    return new ModuleScriptProvider() {
      @Override
      public ModuleScript getModuleScript(Context cx, String moduleId,
          URI moduleUri, URI baseUri, Scriptable paths) throws Exception {
        return JavaScriptExecutor.this.getModuleScript(cx, moduleId, moduleUri,
            baseUri, paths);
      }
    };
  }

  private ModuleScript getModuleScript(Context cx, String moduleId,
      URI moduleUri, URI baseUri, Scriptable paths) throws Exception {
    String path = '/' + name + '/' + moduleId + ".js";
    InputStream script = getClass().getResourceAsStream(path);
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