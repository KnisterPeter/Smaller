package com.sinnerschrader.smaller;

import java.io.File;

import org.apache.camel.Body;
import org.apache.camel.Property;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinnerschrader.minificator.Closure;
import com.sinnerschrader.minificator.ExecutionException;
import com.sinnerschrader.smaller.Manifest.Task;

/**
 * @author marwol
 */
public class TaskHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskHandler.class);

  /**
   * @param main
   * @return the route name of the next step
   */
  public String runTask(@Body Manifest main) {
    Task task = main.getNext();
    if (task == null) {
      return null;
    }
    return "direct:run" + StringUtils.capitalize(task.getProcessor().toLowerCase());
  }

  /**
   * @param base
   * @param main
   * @throws Exception
   */
  public void runClosure(@Property(Router.PROP_DIRECTORY) File base, @Body Manifest main) throws Exception {
    Task task = main.getCurrent();

    Closure closure = new Closure(new com.sinnerschrader.minificator.Logger() {
      public void info(String message) {
        LOGGER.info(message);
      }
    });
    closure.setBaseDir(base);
    closure.setJson(true);
    closure.setClosureSourceFiles(task.getIn());
    closure.setClosureTargetFile(new File(base, task.getOut()[0]));
    try {
      closure.run();
    } catch (ExecutionException e) {
      throw new Exception("Failed to run closure", e);
    }
  }

}
