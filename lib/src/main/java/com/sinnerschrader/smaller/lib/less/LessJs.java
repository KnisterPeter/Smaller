package com.sinnerschrader.smaller.lib.less;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import ro.isdc.wro.extensions.script.RhinoScriptBuilder;

import com.sinnerschrader.smaller.lib.resource.ResourceResolver;

/**
 * @author markusw
 */
public class LessJs {

  /**
   * @param resolver
   * @param input
   * @param output
   * @throws IOException
   */
  public void run(ResourceResolver resolver, Reader input, Writer output)
      throws IOException {
    String data = IOUtils.toString(input);
    data = data.replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\n");

    RhinoScriptBuilder builder = RhinoScriptBuilder.newChain();
    ScriptableObject.putProperty(builder.getScope(), "resolver",
        Context.javaToJS(resolver, builder.getScope()));
    addScript(builder, "less-env.js");
    addScript(builder, "less-1.3.0.js");
    Object result = builder.evaluate("lessIt(\"" + data + "\")", "less.js");
    output.append(String.valueOf(result));
  }

  private void addScript(RhinoScriptBuilder builder, String name)
      throws IOException {
    InputStream script = getClass().getResourceAsStream(name);
    try {
      builder.evaluateChain(script, name);
    } finally {
      IOUtils.closeQuietly(script);
    }
  }

}
