package de.matrixweb.smaller.clients.test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;

import de.matrixweb.smaller.common.Manifest;
import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.common.Version;

/**
 * @author marwol
 */
public class Version_0_6_3_Test extends AbstractBaseTest {

  private static URLClassLoader classloader;

  private static Version clientVersion;

  /**
   * @throws Exception
   */
  @BeforeClass
  public static void setupClassLoader() throws Exception {
    System.out.println("Prepare ClassLoader for 0.6.3 client");
    final File clientJar = new File("./target/smaller-client-ant-0.6.3.jar");
    if (!clientJar.exists()) {
      FileUtils.copyURLToFile(new URL(
          "http://search.maven.org/remotecontent?filepath=de/matrixweb/smaller/ant/0.6.3/ant-0.6.3.jar"), clientJar);
    }
    classloader = new URLClassLoader(new URL[] { clientJar.toURI().toURL() }, null);

    try {
      clientVersion = Version.getVersion(classloader.loadClass("de.matrixweb.smaller.common.Version")
          .getMethod("getCurrentVersion").invoke(null).toString());
    } catch (final ClassNotFoundException e) {
      clientVersion = Version.UNDEFINED;
    }
  }

  /**
   * @see de.matrixweb.smaller.AbstractBaseTest#runToolChain(java.lang.String,
   *      de.matrixweb.smaller.AbstractBaseTest.ToolChainCallback)
   */
  @Override
  protected void runToolChain(final Version minimum, final String file, final ToolChainCallback callback)
      throws Exception {
    if (clientVersion.isAtLeast(minimum)) {
      prepareTestFiles(file, callback, new ExecuteTestCallback() {
        @Override
        public void execute(final Manifest manifest, final File source, final File target) throws Exception {
          final de.matrixweb.smaller.common.Task task = manifest.getCurrent();

          final Task ant = new Task(classloader);
          ant.setProcessor(task.getProcessor());
          ant.setIn(StringUtils.join(task.getIn(), ","));
          ant.setOut(StringUtils.join(task.getOut(), ","));
          ant.setOptions(task.getOptionsDefinition());
          ant.setHost("localhost");
          ant.setPort("1148");
          ant.setFiles(prepareFileSet(source));
          ant.setTarget(target);
          ant.execute();
        }
      });
    }
  }

  private Object prepareFileSet(final File source) throws Exception {
    final Object project = classloader.loadClass("org.apache.tools.ant.Project").newInstance();
    final Object fileSet = classloader.loadClass("org.apache.tools.ant.types.FileSet").newInstance();
    fileSet.getClass().getMethod("setProject", project.getClass()).invoke(fileSet, project);
    fileSet.getClass().getMethod("setDir", source.getClass()).invoke(fileSet, source);
    return fileSet;
  }

  private static class Task {

    private final Object task;

    Task(final ClassLoader cl) throws Exception {
      final Class<?> clazz = cl.loadClass("de.matrixweb.smaller.clients.ant.SmallerTask");
      this.task = clazz.newInstance();
    }

    void setProcessor(final String processor) throws Exception {
      this.task.getClass().getMethod("setProcessor", String.class).invoke(this.task, processor);
    }

    void setIn(final String in) throws Exception {
      this.task.getClass().getMethod("setIn", String.class).invoke(this.task, in);
    }

    void setOut(final String out) throws Exception {
      this.task.getClass().getMethod("setOut", String.class).invoke(this.task, out);
    }

    void setOptions(final String options) throws Exception {
      this.task.getClass().getMethod("setOptions", String.class).invoke(this.task, options);
    }

    void setHost(final String host) throws Exception {
      this.task.getClass().getMethod("setHost", String.class).invoke(this.task, host);
    }

    void setPort(final String port) throws Exception {
      this.task.getClass().getMethod("setPort", String.class).invoke(this.task, port);
    }

    void setFiles(final Object files) throws Exception {
      this.task.getClass().getMethod("setFiles", files.getClass()).invoke(this.task, files);
    }

    void setTarget(final File target) throws Exception {
      this.task.getClass().getMethod("setTarget", File.class).invoke(this.task, target);
    }

    void execute() throws Exception {
      try {
        this.task.getClass().getMethod("execute").invoke(this.task);
      } catch (final InvocationTargetException e) {
        final Throwable t = e.getTargetException();
        if ("SmallerException".equals(t.getClass().getSimpleName())) {
          throw new SmallerException(t.getMessage(), e);
        }
        throw e;
      }
    }

  }

}
