package de.matrixweb.smaller.ui.base.internal;

import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * @author marwol
 */
public class EntryPoint extends AbstractEntryPoint {

  /**
   * @see org.eclipse.rap.rwt.application.AbstractEntryPoint#createContents(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void createContents(final Composite parent) {
    parent.setLayout(new FillLayout());
    TestComp comp = new TestComp(parent, SWT.NONE);
  }

}
