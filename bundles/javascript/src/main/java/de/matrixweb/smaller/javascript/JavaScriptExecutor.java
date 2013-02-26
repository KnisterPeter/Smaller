package de.matrixweb.smaller.javascript;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

/**
 * @author marwol
 */
public interface JavaScriptExecutor {

  /**
   * @param name
   *          The name to use of method to make available
   * @param object
   *          The object to make globally available in the environment
   */
  void addGlobalFunction(String name, Object object);

  /**
   * @param name
   *          The name to use of method to make available
   * @param object
   *          The object to make globally available in the environment
   * @param method
   *          The name of the method to publish
   */
  void addGlobalFunction(String name, Object object, String method);

  /**
   * @param source
   *          The source code of a script to add
   * @param name
   *          The name of the source for debugging/error reporting
   */
  void addScriptSource(String source, String name);

  /**
   * @param file
   *          The path of the file to add
   */
  void addScriptFile(String file);

  /**
   * @param url
   *          The {@link URL} to load the script from
   */
  void addScriptFile(URL url);

  /**
   * @param source
   *          The source script to evaluate. This should contain <code>%s</code>
   *          where the reader input should be placed.
   */
  void addCallScript(String source);

  /**
   * @param input
   *          The execution input parameters
   * @param output
   *          The execution result
   * @throws IOException
   */
  void run(Reader input, Writer output) throws IOException;

  /**
   * 
   */
  void dispose();

}
