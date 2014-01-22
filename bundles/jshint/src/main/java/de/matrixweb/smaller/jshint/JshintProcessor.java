package de.matrixweb.smaller.jshint;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;

import de.matrixweb.smaller.javascript.JavaScriptExecutor;
import de.matrixweb.smaller.javascript.JavaScriptExecutorFast;
import de.matrixweb.smaller.jshint.JshintProcessor.JsHintResult.JsHintError;
import de.matrixweb.smaller.resource.MultiResourceProcessor;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.ResourceGroup;
import de.matrixweb.smaller.resource.Type;
import de.matrixweb.vfs.VFS;

/**
 * @author markusw
 */
public class JshintProcessor implements MultiResourceProcessor {

  private final JavaScriptExecutor executor;

  /**
   * 
   */
  public JshintProcessor() {
    this.executor = new JavaScriptExecutorFast("jshint-1.1.0", 9,
        JshintProcessor.class);
    this.executor.addScriptFile(getClass().getResource(
        "/jshint-1.1.0/jshint-1.1.0.js"));
    this.executor.addScriptFile(getClass().getResource(
        "/jshint-1.1.0/jshint-call.js"));
    this.executor.addCallScript("hint(%s);");
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#supportsType(de.matrixweb.smaller.resource.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.JS;
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.vfs.VFS,
   *      de.matrixweb.smaller.resource.Resource, java.util.Map)
   */
  @Override
  public Resource execute(final VFS vfs, final Resource resource,
      final Map<String, Object> options) throws IOException {
    final List<String> errors = new ArrayList<String>();
    if (resource instanceof ResourceGroup) {
      for (final Resource res : ((ResourceGroup) resource).getResources()) {
        errors.addAll(scanResource(res, options));
      }
    } else {
      errors.addAll(scanResource(resource, options));
    }
    handleErrors(errors);
    return resource;
  }

  private List<String> scanResource(final Resource resource,
      final Map<String, Object> options) throws IOException {
    final List<String> results = new ArrayList<String>();

    final Map<String, Object> params = new HashMap<String, Object>();
    params.put("source", resource.getContents());
    params.put("options", options);
    final StringWriter writer = new StringWriter();
    this.executor
        .run(new StringReader(new ObjectMapper().writeValueAsString(params)),
            writer);
    final JsHintResult result = new ObjectMapper().readValue(writer.toString(),
        JsHintResult.class);
    if (result.errors != null) {
      for (final JsHintError error : result.errors) {
        results.add(resource.getPath() + " line " + error.line + ": "
            + error.reason + "\n\t" + error.evidence);
      }
    }

    return results;
  }

  private void handleErrors(final List<String> errors) {
    final List<String> copy = new ArrayList<String>(errors);
    final StringBuilder sb = new StringBuilder();
    if (copy.size() > 0) {
      sb.append(copy.get(0));
      copy.remove(0);
    }
    for (final String error : copy) {
      sb.append('\n').append(error);
    }
    if (sb.length() > 0) {
      throw new JsHintException('\n' + sb.toString());
    }
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#dispose()
   */
  @Override
  public void dispose() {
    this.executor.dispose();
  }

  /**
   * Typesafe wrapper for the jshint json result
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  static class JsHintResult {

    public List<Map<String, Object>> functions;

    public Map<String, Object> options;

    public List<JsHintError> errors;

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class JsHintError {

      public String evidence;

      public String line;

      public String reason;

    }

  }

}
