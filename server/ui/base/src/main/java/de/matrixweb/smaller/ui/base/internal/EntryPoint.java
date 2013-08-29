package de.matrixweb.smaller.ui.base.internal;

import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author marwol
 */
public class EntryPoint extends AbstractEntryPoint {

  /**
   * @see org.eclipse.rap.rwt.application.AbstractEntryPoint#createContents(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void createContents(final Composite parent) {
    Label label = new Label(parent, SWT.NONE);
    label.setText("Hello RAP World");
  }

}
