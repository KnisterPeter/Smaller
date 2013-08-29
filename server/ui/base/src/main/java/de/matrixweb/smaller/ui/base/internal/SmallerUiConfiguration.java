package de.matrixweb.smaller.ui.base.internal;

import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;

/**
 * @author marwol
 */
public class SmallerUiConfiguration implements ApplicationConfiguration {

  /**
   * @see org.eclipse.rap.rwt.application.ApplicationConfiguration#configure(org.eclipse.rap.rwt.application.Application)
   */
  @Override
  public void configure(final Application application) {
    application.addEntryPoint("/ui", EntryPoint.class, null);
  }

}
