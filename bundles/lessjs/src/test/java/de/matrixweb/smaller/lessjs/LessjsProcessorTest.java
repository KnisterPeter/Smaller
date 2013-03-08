package de.matrixweb.smaller.lessjs;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import de.matrixweb.smaller.javascript.JavaScriptExecutor;
import de.matrixweb.smaller.resource.Type;

/**
 * @author markusw
 */
public class LessjsProcessorTest {

  /** */
  @Test
  public void testProcessorCreation() {
    final JavaScriptExecutor executor = mock(JavaScriptExecutor.class);
    new LessjsProcessor("version", executor);
    verify(executor).addGlobalFunction(eq("resolve"), anyObject());
  }

  /** */
  @Test
  public void testTypeSupport() {
    final JavaScriptExecutor executor = mock(JavaScriptExecutor.class);
    final LessjsProcessor proc = new LessjsProcessor("version", executor);
    assertThat(proc.supportsType(Type.JS), is(false));
    assertThat(proc.supportsType(Type.CSS), is(true));
  }

}
