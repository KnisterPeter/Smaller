package de.matrixweb.smaller.clients.maven;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import de.matrixweb.smaller.clients.common.ExecutionException;
import de.matrixweb.smaller.common.Manifest;
import de.matrixweb.smaller.common.Version;
import de.matrixweb.smaller.config.ConfigFile;
import de.matrixweb.smaller.pipeline.Pipeline;
import de.matrixweb.smaller.resource.ProcessorFactory;
import de.matrixweb.smaller.resource.ResourceResolver;
import de.matrixweb.smaller.resource.VFSResourceResolver;
import de.matrixweb.smaller.resource.impl.JavaEEProcessorFactory;
import de.matrixweb.vfs.Logger;
import de.matrixweb.vfs.VFS;
import de.matrixweb.vfs.wrapped.JavaFile;

/**
 * @author marwol
 * @goal smaller
 * @phase process-resources
 */
public class SmallerStandaloneMojo extends AbstractMojo {

  /**
   * The server host to connect to.
   * 
   * @parameter default-value="sr.s2.de"
   */
  private String host;

  /**
   * The server port to connect to.
   * 
   * @parameter default-value="80"
   */
  private String port;

  /**
   * The proxy host to connect to.
   */
  private final String proxyhost = null;

  /**
   * The proxy port to connect to.
   */
  private final String proxyport = null;

  /**
   * The target folder.
   * 
   * @parameter
   */
  private File target;

  /**
   * @parameter alias="config-file" default-value="${basedir}/smaller.yml"
   */
  private File configFile;

  /**
   * @param target
   *          the target to set
   */
  public void setTarget(final String target) {
    this.target = new File(target);
  }

  /**
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("Write result to " + this.target);
    final SmallerClient client = new SmallerClient(getLog(), this.host,
        this.port, this.proxyhost, this.proxyport, this.target, this.configFile) {
      @Override
      protected void executeSmaller(final File base,
          final List<String> includedFiles, final File target,
          final String host, final String port, final String proxyhost,
          final String proxyport, final ConfigFile configFile)
          throws ExecutionException {
        final ProcessorFactory processorFactory = new JavaEEProcessorFactory();
        try {
          final VFS vfs = new VFS(new Logger() {
            @Override
            public void debug(final String message) {
              getLog().debug(message);
            }

            @Override
            public void info(final String messsage) {
              getLog().info(messsage);
            }

            @Override
            public void error(final String message, final Exception e) {
              getLog().error(message, e);
            }
          });
          try {
            getLog().info("MVN: Adding " + base + " to VFS");
            vfs.mount(vfs.find("/"), new JavaFile(base));
            final ResourceResolver resolver = new VFSResourceResolver(vfs);
            final Manifest manifest = Manifest.fromConfigFile(configFile);
            final Pipeline pipeline = new Pipeline(processorFactory);
            pipeline.execute(Version.getCurrentVersion(), vfs, resolver,
                manifest, target);
          } finally {
            vfs.dispose();
          }
        } catch (final IOException e) {
          throw new ExecutionException("Embedded smaller failed", e);
        } finally {
          processorFactory.dispose();
        }
      }
    };
    client.execute();
  }

}
