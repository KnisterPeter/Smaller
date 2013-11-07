package de.matrixweb.smaller.lessjs;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import de.matrixweb.smaller.resource.Type;

/**
 * @author markusw
 */
public class LessjsProcessorTest {

  /** */
  @Test
  public void testTypeSupport() {
    final LessjsProcessor proc = new LessjsProcessor("version");
    assertThat(proc.supportsType(Type.JS), is(false));
    assertThat(proc.supportsType(Type.CSS), is(true));
  }

}
