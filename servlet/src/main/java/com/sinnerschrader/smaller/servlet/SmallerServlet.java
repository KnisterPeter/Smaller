package com.sinnerschrader.smaller.servlet;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author marwol
 */
public class SmallerServlet extends HttpServlet {

  private static final long serialVersionUID = -7622385125982337921L;

  /**
   * @see javax.servlet.GenericServlet#init()
   */
  @Override
  public void init() throws ServletException {
    String processor = getInitParameter("processor");
    String[] includes = getInitParameter("includes").split("[, ]");
    String[] excludes = getInitParameter("excludes").split("[, ]");
    Set<String> resources = new ResourceScanner(getServletContext(), includes, excludes).getResources();
  }

  /**
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
  }

}
