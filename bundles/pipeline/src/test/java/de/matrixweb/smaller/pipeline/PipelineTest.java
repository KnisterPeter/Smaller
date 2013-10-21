package de.matrixweb.smaller.pipeline;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.common.Task;
import de.matrixweb.smaller.common.Version;
import de.matrixweb.smaller.resource.ProcessorFactory;
import de.matrixweb.smaller.resource.ResourceResolver;
import de.matrixweb.smaller.resource.vfs.VFS;

/**
 * @author markusw
 */
public class PipelineTest {

  private Pipeline pipeline;

  private ProcessorFactory processorFactory;

  private ResourceResolver resourceResolver;

  /** */
  @Before
  public void setUp() {
    this.processorFactory = mock(ProcessorFactory.class);
    this.resourceResolver = mock(ResourceResolver.class);

    this.pipeline = new Pipeline(this.processorFactory);
  }

  /** */
  @Test
  public void testOutputTypeExistOnlyOnce() {
    final VFS vfs = new VFS();
    try {
      final Task task = new Task();
      task.setIn(new String[] {});
      task.setOut(new String[] { "test.js", "test2.js" });

      this.pipeline.execute(Version._1_0_0, vfs, this.resourceResolver, task);
    } catch (final SmallerException e) {
      assertThat(e.getMessage(), is("Each output type must exist only once"));
    } finally {
      vfs.dispose();
    }
  }

}
