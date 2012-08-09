package com.sinnerschrader.smaller.lib.processors;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import ro.isdc.wro.extensions.script.RhinoScriptBuilder;

import com.sinnerschrader.smaller.lib.ProcessorChain.Type;
import com.sinnerschrader.smaller.lib.resource.Resource;
import com.sinnerschrader.smaller.lib.resource.ResourceResolver;
import com.sinnerschrader.smaller.lib.resource.StringResource;

/**
 * @author markusw
 */
public class LessjsProcessor implements Processor {

  /**
   * @see com.sinnerschrader.smaller.lib.processors.Processor#supportsType(com.sinnerschrader.smaller.lib.ProcessorChain.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.CSS;
  }

  /**
   * @see com.sinnerschrader.smaller.lib.processors.Processor#execute(com.sinnerschrader.smaller.lib.resource.Resource)
   */
  @Override
  public Resource execute(final Resource resource) throws IOException {
    final StringWriter writer = new StringWriter();
    executeLessJs(resource.getResolver(),
        new StringReader(resource.getContents()), writer);
    return new StringResource(resource.getResolver(), resource.getType(),
        resource.getPath(), writer.toString());
  }

  private void executeLessJs(ResourceResolver resolver, Reader input,
      Writer output) throws IOException {
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
