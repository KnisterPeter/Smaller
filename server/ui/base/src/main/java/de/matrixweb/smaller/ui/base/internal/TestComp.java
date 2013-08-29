package de.matrixweb.smaller.ui.base.internal;

import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.rap.rwt.widgets.DialogUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import swing2swt.layout.BorderLayout;

/**
 * @author marwol
 */
public class TestComp extends Composite {
  private final Label lblSomeLabel;
  private final Text text;

  /**
   * Create the composite.
   * 
   * @param parent
   * @param style
   */
  public TestComp(final Composite parent, final int style) {
    super(parent, style);
    setLayout(new BorderLayout(0, 0));

    this.lblSomeLabel = new Label(this, SWT.CENTER);
    this.lblSomeLabel.setLayoutData(BorderLayout.NORTH);
    this.lblSomeLabel.setText("Some Label");

    Button btnButton = new Button(this, SWT.NONE);
    btnButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        MessageBox box = new MessageBox(parent.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        box.setMessage("Some stupid question?");
        DialogUtil.open(box, new DialogCallback() {
          @Override
          public void dialogClosed(final int returnCode) {
            TestComp.this.lblSomeLabel.setText("Pressed: " + (returnCode == SWT.YES ? "yes" : "no"));
          }
        });
      }
    });
    btnButton.setLayoutData(BorderLayout.EAST);
    btnButton.setText("Button");

    Composite composite = new Composite(this, SWT.NONE);
    composite.setLayoutData(BorderLayout.CENTER);
    composite.setLayout(new FillLayout(SWT.VERTICAL));

    this.text = new Text(composite, SWT.BORDER);

    Spinner spinner = new Spinner(composite, SWT.BORDER);

    Button btnCheckButton = new Button(composite, SWT.CHECK);
    btnCheckButton.setText("Check Button");

    // Browser browser = new Browser(this, SWT.NONE);
    // browser.setUrl("http://www.sinnerschrader.com");
    // browser.setLayoutData(BorderLayout.CENTER);

  }

  @Override
  protected void checkSubclass() {
    // Disable the check that prevents subclassing of SWT components
  }

}
