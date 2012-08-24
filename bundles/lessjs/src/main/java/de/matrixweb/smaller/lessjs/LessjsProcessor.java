package de.matrixweb.smaller.lessjs;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;


import de.matrixweb.smaller.javascript.JavaScriptExecutor;
import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.ResourceResolver;
import de.matrixweb.smaller.resource.StringResource;
import de.matrixweb.smaller.resource.Type;

/**
 * @author markusw
 */
public class LessjsProcessor implements Processor {

  ProxyResourceResolver proxy = new ProxyResourceResolver();

  private final JavaScriptExecutor executor;

  /**
   * 
   */
  public LessjsProcessor() {
    this.executor = new JavaScriptExecutor("less-1.3.0");
    this.executor.addProperty("resolver", this.proxy);
    this.executor.addScriptFile("/lessjs-1.3.0/less-env.js");
    this.executor.addScriptFile("/lessjs-1.3.0/less-1.3.0.js");
    this.executor.addCallScript("lessIt(%s);");
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#supportsType(de.matrixweb.smaller.resource.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.CSS;
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.smaller.resource.Resource)
   */
  @Override
  public Resource execute(final Resource resource) throws IOException {
    final StringWriter writer = new StringWriter();

    this.proxy.setResolver(resource.getResolver());
    try {
      this.executor.run(new StringReader(resource.getContents()), writer);
    } finally {
      this.proxy.removeResolver();
    }

    return new StringResource(resource.getResolver(), resource.getType(),
        resource.getPath(), writer.toString());
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

    private void setResolver(final ResourceResolver resolver) {
      this.resolver.set(resolver);
    }

    private void removeResolver() {
      this.resolver.remove();
    }

  }

}
