package com.sinnerschrader.smaller.resource.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinnerschrader.smaller.resource.Processor;
import com.sinnerschrader.smaller.resource.ProcessorFactory;

/**
 * @author markusw
 */
public class JavaEEProcessorFactory implements ProcessorFactory {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(JavaEEProcessorFactory.class);

  private Map<String, Processor> processors = new HashMap<String, Processor>();

  /**
   * @see com.sinnerschrader.smaller.resource.ProcessorFactory#getProcessor(java.lang.String)
   */
  @Override
  public Processor getProcessor(String name) {
    Processor processor = processors.get(name);
    if (processor == null) {
      try {
        processor = (Processor) Class.forName(
            "com.sinnerschrader.smaller.lib.processors."
                + StringUtils.capitalize(name.toLowerCase()) + "Processor")
            .newInstance();
        processors.put(name, processor);
      } catch (final InstantiationException e) {
        LOGGER.warn("Ignoring invalid processor " + name, e);
      } catch (final IllegalAccessException e) {
        LOGGER.warn("Ignoring invalid processor " + name, e);
      } catch (final ClassNotFoundException e) {
        try {
          // TODO: This should be replaced by some classpath scanner
          processor = (Processor) Class.forName(
              "com.sinnerschrader.smaller." + name.toLowerCase() + "."
                  + StringUtils.capitalize(name.toLowerCase()) + "Processor")
              .newInstance();
          processors.put(name, processor);
        } catch (ClassNotFoundException e1) {
          LOGGER.warn("Ignoring invalid processor " + name, e);
        } catch (InstantiationException e1) {
          LOGGER.warn("Ignoring invalid processor " + name, e);
        } catch (IllegalAccessException e1) {
          LOGGER.warn("Ignoring invalid processor " + name, e);
        }
      }
    }
    return processor;
  }

  /**
   * @see com.sinnerschrader.smaller.resource.ProcessorFactory#getProcessor(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public Processor getProcessor(String name, String version) {
    return getProcessor(name);
  }
}
