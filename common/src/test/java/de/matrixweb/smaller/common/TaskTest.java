package de.matrixweb.smaller.common;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author marwol
 */
public class TaskTest {

  /** */
  @Test
  public void testInAndOut() {
    final String[] in0 = new String[] { "a" };
    final String[] out0 = new String[] { "b" };
    final Task task = new Task("", in0, out0);
    final String[] in1 = task.getIn();
    final String[] out1 = task.getOut();
    assertThat(Integer.valueOf(in1.length), is(Integer.valueOf(in0.length)));
    assertThat(in1[0], is(in0[0]));
    assertThat(Integer.valueOf(out1.length), is(Integer.valueOf(out0.length)));
    assertThat(out1[0], is(out0[0]));
  }

}
