package de.matrixweb.smaller.lessjs;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.matrixweb.nodejs.NodeJsExecutor;
import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.javascript.JavaScriptExecutor;
import de.matrixweb.smaller.javascript.JavaScriptExecutorFast;
import de.matrixweb.smaller.resource.MergingProcessor;
import de.matrixweb.smaller.resource.ProcessorUtil;
import de.matrixweb.smaller.resource.ProcessorUtil.ProcessorCallback;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.ResourceGroup;
import de.matrixweb.smaller.resource.ResourceResolver;
import de.matrixweb.smaller.resource.Type;
import de.matrixweb.vfs.VFS;

/**
 * @author markusw
 */
public class LessjsProcessor implements MergingProcessor {

  private static final String WIN_LOC_HREF_FIX = "protocol://host:port/";

  private final String version;

  private final ProxyResourceResolver proxy = new ProxyResourceResolver();

  private JavaScriptExecutor executor;

  private NodeJsExecutor node;

  /**
   * 
   */
  public LessjsProcessor() {
    this("1.6.1");
  }

  /**
   * @param version
   */
  public LessjsProcessor(final String version) {
    this.version = version;
  }

  private boolean runWithNode() {
    try {
      return Integer.parseInt(this.version.substring(2, 3)) > 4;

    } catch (final NumberFormatException e) {
      return false;
    }
  }

  private void configureWithNode() {
    if (this.node == null) {
      try {
        this.node = new NodeJsExecutor();
        this.node.setModule(getClass().getClassLoader(), "lessjs-"
            + this.version);
      } catch (final IOException e) {
        throw new SmallerException("Failed to conigure node for lessjs-"
            + this.version, e);
      }
    }
  }

  private void configureWithJs() {
    if (this.executor == null) {
      this.executor = new JavaScriptExecutorFast("less-" + this.version, 9,
          LessjsProcessor.class);
      this.executor
          .addGlobalFunction("resolve", new ResolveFunctor(this.proxy));
      this.executor.addScriptSource("win_loc_href_fix = '" + WIN_LOC_HREF_FIX
          + "';", "win_loc_href_fix");
      this.executor
          .addScriptFile(getClass().getResource("/lessjs/less-env.js"));
      this.executor.addScriptFile(getClass().getResource(
          "/lessjs/less-" + this.version + ".js"));
      this.executor.addCallScript("lessIt(%s);");
    }
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#supportsType(de.matrixweb.smaller.resource.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.CSS;
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.vfs.VFS,
   *      de.matrixweb.smaller.resource.Resource, java.util.Map)
   */
  @Override
  public Resource execute(final VFS vfs, final Resource resource,
      final Map<String, Object> options) throws IOException {
    if (runWithNode()) {
      configureWithNode();
    } else {
      configureWithJs();
    }

    List<Resource> resources = null;
    if (resource instanceof ResourceGroup) {
      resources = ((ResourceGroup) resource).getResources();
    } else {
      resources = Arrays.asList(resource);
    }
    final Resource input = resources.get(0);

    if (runWithNode()) {
      return input.getResolver().resolve(
          '/' + this.node.run(vfs, input.getPath(), options));
    } else {
      return ProcessorUtil.process(vfs, input, "less", "css",
          new ProcessorCallback() {
            @Override
            public void call(final Reader reader, final Writer writer)
                throws IOException {
              LessjsProcessor.this.proxy.setResolver(input.getResolver());
              try {
                final StringWriter tempWriter = new StringWriter();
                LessjsProcessor.this.executor.run(
                    new StringReader(input.getContents()), tempWriter);
                writer.write(tempWriter.toString()
                    .replace(WIN_LOC_HREF_FIX, ""));
              } finally {
                LessjsProcessor.this.proxy.removeResolver();
              }
            }
          });
    }
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#dispose()
   */
  @Override
  public void dispose() {
    if (this.node != null) {
      this.node.dispose();
    }
    if (this.executor != null) {
      this.executor.dispose();
    }
  }

  /** */
  public static class ResolveFunctor {

    private final ResourceResolver resolver;

    private ResolveFunctor(final ResourceResolver resolver) {
      this.resolver = resolver;
    }

    /**
     * @param input
     * @return Returns the resolved {@link Resource} content
     */
    public String resolve(final String input) {
      try {
        return this.resolver.resolve(input.replace(WIN_LOC_HREF_FIX, ""))
            .getContents();
      } catch (final IOException e) {
        throw new SmallerException(
            "Failed to resolve resource '" + input + "'", e);
      }
    }

  }

  private static class ProxyResourceResolver implements ResourceResolver {

    private final ThreadLocal<ResourceResolver> resolver = new ThreadLocal<ResourceResolver>();

    /**
     * @see de.matrixweb.smaller.resource.ResourceResolver#resolve(java.lang.String)
     */
    @Override
    public Resource resolve(final String path) {
      return this.resolver.get().resolve(path);
    }

    /**
     * @see de.matrixweb.smaller.resource.ResourceResolver#writeAll()
     */
    @Override
    public File writeAll() {
      throw new UnsupportedOperationException();
    }

    private void setResolver(final ResourceResolver resolver) {
      this.resolver.set(resolver);
    }

    private void removeResolver() {
      this.resolver.remove();
    }

  }

}
