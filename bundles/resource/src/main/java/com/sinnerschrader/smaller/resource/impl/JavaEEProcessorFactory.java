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

  private final Map<String, Processor> processors = new HashMap<String, Processor>();

  /**
   * @see com.sinnerschrader.smaller.resource.ProcessorFactory#getProcessor(java.lang.String)
   */
  @Override
  public Processor getProcessor(final String name) {
    Processor processor = this.processors.get(name);
    if (processor == null) {
      final String lname = name.toLowerCase();
      final String pname = StringUtils.capitalize(lname) + "Processor";
      try {
        processor = (Processor) Class.forName(
            "com.sinnerschrader.smaller." + lname + "." + pname).newInstance();
        this.processors.put(name, processor);
      } catch (final InstantiationException e) {
        LOGGER.warn("Ignoring invalid processor " + name, e);
      } catch (final IllegalAccessException e) {
        LOGGER.warn("Ignoring invalid processor " + name, e);
      } catch (final ClassNotFoundException e) {
        LOGGER.warn("Ignoring invalid processor " + name, e);
      }
    }
    return processor;
  }

  /**
   * @see com.sinnerschrader.smaller.resource.ProcessorFactory#getProcessor(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public Processor getProcessor(final String name, final String version) {
    return getProcessor(name);
  }
}
