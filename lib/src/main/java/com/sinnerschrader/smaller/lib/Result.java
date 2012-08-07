package com.sinnerschrader.smaller.lib;

import com.sinnerschrader.smaller.lib.resource.Resource;

/**
 * @author marwol
 */
public class Result {

  private Resource js;

  private Resource css;

  /**
   * @param js
   * @param css
   */
  public Result(final Resource js, final Resource css) {
    this.js = js;
    this.css = css;
  }

  /**
   * @return the js
   */
  public final Resource getJs() {
    return this.js;
  }

  /**
   * @param js
   *          the js to set
   */
  public final void setJs(final Resource js) {
    this.js = js;
  }

  /**
   * @return the css
   */
  public final Resource getCss() {
    return this.css;
  }

  /**
   * @param css
   *          the css to set
   */
  public final void setCss(final Resource css) {
    this.css = css;
  }

}