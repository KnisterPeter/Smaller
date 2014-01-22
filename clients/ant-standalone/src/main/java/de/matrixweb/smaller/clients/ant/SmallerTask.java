package de.matrixweb.smaller.clients.ant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import de.matrixweb.smaller.clients.common.Util;
import de.matrixweb.smaller.common.Manifest;
import de.matrixweb.smaller.common.Version;
import de.matrixweb.smaller.config.ConfigFile;
import de.matrixweb.smaller.config.Environment;
import de.matrixweb.smaller.pipeline.Pipeline;
import de.matrixweb.smaller.resource.ProcessorFactory;
import de.matrixweb.smaller.resource.ResourceResolver;
import de.matrixweb.smaller.resource.VFSResourceResolver;
import de.matrixweb.smaller.resource.impl.JavaEEProcessorFactory;
import de.matrixweb.vfs.VFS;
import de.matrixweb.vfs.wrapped.JavaFile;

/**
 * @author marwol
 */
public class SmallerTask extends Task {

  private File target;

  private File configFilePath;

  /**
   * @param target
   *          the target to set
   */
  public final void setTarget(final File target) {
    this.target = target;
  }

  /**
   * @param configFile
   *          the configFile to set
   */
  public void setConfigFile(final File configFile) {
    this.configFilePath = configFile;
  }

  /**
   * @see org.apache.tools.ant.Task#execute()
   */
  @Override
  public void execute() {
    final ProcessorFactory processorFactory = new JavaEEProcessorFactory();
    try {
      final File temp = File.createTempFile("smaller-ant", ".dir");
      try {
        temp.delete();
        temp.mkdirs();

        log("Reading config-file: " + this.configFilePath);
        final ConfigFile configFile = ConfigFile.read(this.configFilePath);

        final List<String> includedFiles = new ArrayList<String>();
        for (final Environment env : configFile.getEnvironments().values()) {
          for (final String dir : env.getFiles().getFolder()) {
            copyFirstInputFile(env, dir, temp);

            File base = new File(dir);
            if (!base.isAbsolute()) {
              base = new File(this.configFilePath.getParentFile(),
                  base.getPath());
            }
            final String[] included = scanIncludedFiles(base.getAbsolutePath(),
                env.getFiles().getIncludes(), env.getFiles().getExcludes());

            for (final String include : included) {
              FileUtils.copyFile(new File(base, include), new File(temp,
                  include));
              includedFiles.add(include);
            }
          }
        }

        final VFS vfs = new VFS();
        try {
          vfs.mount(vfs.find("/"), new JavaFile(temp));
          final ResourceResolver resolver = new VFSResourceResolver(vfs);

          final Manifest manifest = new Util(null)
              .convertConfigFileToManifest(configFile);
          final Pipeline pipeline = new Pipeline(processorFactory);
          pipeline.execute(Version.getCurrentVersion(), vfs, resolver,
              manifest, this.target);
        } finally {
          vfs.dispose();
        }

      } finally {
        FileUtils.deleteDirectory(temp);
      }
    } catch (final IOException e) {
      log(Util.formatException(e), Project.MSG_ERR);
      throw new BuildException("Failed execute embedded smaller", e);
    } finally {
      processorFactory.dispose();
    }
  }

  private void copyFirstInputFile(final Environment env, final String dir,
      final File temp) throws IOException {
    final String input = env.getProcessors().get(env.getPipeline()[0]).getSrc();
    if (input != null) {
      File inputFile = new File(dir, input);
      if (!inputFile.isAbsolute()) {
        inputFile = new File(this.configFilePath.getParentFile(),
            inputFile.getPath());
      }

      if (inputFile.exists()) {
        FileUtils.copyFile(inputFile, new File(temp, input));
      }
    }
  }

  private String[] scanIncludedFiles(final String dir, final String[] includes,
      final String[] excludes) {
    final FileSet set = new FileSet();
    log("Scanning " + dir);
    set.setProject(getProject());
    set.setDir(new File(dir));
    set.appendIncludes(includes);
    set.appendExcludes(excludes);
    return set.getDirectoryScanner().getIncludedFiles();
  }

}
