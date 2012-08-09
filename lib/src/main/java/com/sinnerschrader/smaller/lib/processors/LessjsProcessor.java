package com.sinnerschrader.smaller.lib.processors;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import com.sinnerschrader.smaller.lib.JavaScriptExecutor;
import com.sinnerschrader.smaller.lib.ProcessorChain.Type;
import com.sinnerschrader.smaller.lib.resource.Resource;
import com.sinnerschrader.smaller.lib.resource.ResourceResolver;
import com.sinnerschrader.smaller.lib.resource.StringResource;

/**
 * @author markusw
 */
public class LessjsProcessor implements Processor {

  ProxyResourceResolver proxy = new ProxyResourceResolver();

  private JavaScriptExecutor executor;

  /**
   * 
   */
  public LessjsProcessor() {
    executor = new JavaScriptExecutor("less-1.3.0");
    executor.addProperty("resolver", proxy);
    executor.addScriptFile("/lessjs-1.3.0/less-env.js");
    executor.addScriptFile("/lessjs-1.3.0/less-1.3.0.js");
    executor.addCallScript("lessIt(%s);");
  }

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

    proxy.setResolver(resource.getResolver());
    try {
      executor.run(new StringReader(resource.getContents()), writer);
    } finally {
      proxy.removeResolver();
    }

    return new StringResource(resource.getResolver(), resource.getType(),
        resource.getPath(), writer.toString());
  }

  private static class ProxyResourceResolver implements ResourceResolver {

    private ThreadLocal<ResourceResolver> resolver = new ThreadLocal<ResourceResolver>();

    /**
     * @see com.sinnerschrader.smaller.lib.resource.ResourceResolver#resolve(java.lang.String)
     */
    @Override
    public Resource resolve(String path) {
      return resolver.get().resolve(path);
    }

    private void setResolver(ResourceResolver resolver) {
      this.resolver.set(resolver);
    }

    private void removeResolver() {
      resolver.remove();
    }

  }

}
