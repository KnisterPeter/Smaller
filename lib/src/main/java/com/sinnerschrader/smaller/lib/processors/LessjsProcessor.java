package com.sinnerschrader.smaller.lib.processors;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import com.sinnerschrader.smaller.javascript.JavaScriptExecutor;
import com.sinnerschrader.smaller.resource.Processor;
import com.sinnerschrader.smaller.resource.Resource;
import com.sinnerschrader.smaller.resource.ResourceResolver;
import com.sinnerschrader.smaller.resource.StringResource;
import com.sinnerschrader.smaller.resource.Type;

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
   * @see com.sinnerschrader.smaller.resource.Processor#supportsType(com.sinnerschrader.smaller.resource.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.CSS;
  }

  /**
   * @see com.sinnerschrader.smaller.resource.Processor#canMerge()
   */
  @Override
  public boolean canMerge() {
    return false;
  }

  /**
   * @see com.sinnerschrader.smaller.resource.Processor#execute(com.sinnerschrader.smaller.resource.Resource)
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
     * @see com.sinnerschrader.smaller.resource.ResourceResolver#resolve(java.lang.String)
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
