package de.matrixweb.smaller.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author marwol
 */
public class SmallerServlet extends HttpServlet {

  private static final long serialVersionUID = -7622385125982337921L;

  private EmbeddedSmaller smaller;

  /**
   * @see javax.servlet.GenericServlet#init()
   */
  @Override
  public void init() throws ServletException {
    this.smaller = new EmbeddedSmaller(getServletContext());
    this.smaller.init();
  }

  /**
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doGet(final HttpServletRequest request,
      final HttpServletResponse response) throws ServletException, IOException {
    this.smaller.execute(request, response);
  }

}
