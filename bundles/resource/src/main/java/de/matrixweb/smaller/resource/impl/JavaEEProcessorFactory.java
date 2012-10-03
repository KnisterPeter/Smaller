package de.matrixweb.smaller.resource.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.ProcessorFactory;

/**
 * @author markusw
 */
public class JavaEEProcessorFactory implements ProcessorFactory {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(JavaEEProcessorFactory.class);

  private final Map<String, Processor> processors = new HashMap<String, Processor>();

  /**
   * @see de.matrixweb.smaller.resource.ProcessorFactory#getProcessor(java.lang.String)
   */
  @Override
  public Processor getProcessor(final String name) {
    Processor processor = this.processors.get(name);
    if (processor == null) {
      final String lname = name.toLowerCase();
      final String pname = StringUtils.capitalize(lname) + "Processor";
      try {
        processor = (Processor) Class.forName(
            "de.matrixweb.smaller." + lname + "." + pname).newInstance();
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
   * @see de.matrixweb.smaller.resource.ProcessorFactory#getProcessor(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public Processor getProcessor(final String name, final String version) {
    final String lname = name.toLowerCase();
    final String pname = StringUtils.capitalize(lname) + "Processor";
    try {
      @SuppressWarnings("unchecked")
      final Class<Processor> clazz = (Class<Processor>) Class
          .forName("de.matrixweb.smaller." + lname + "." + pname);
      try {
        final Constructor<Processor> constructor = clazz
            .getConstructor(String.class);
        return constructor.newInstance(version);
      } catch (final NoSuchMethodException e) {
        return clazz.newInstance();
      }
    } catch (final InvocationTargetException e) {
      LOGGER.warn("Ignoring invalid processor " + name, e);
    } catch (final InstantiationException e) {
      LOGGER.warn("Ignoring invalid processor " + name, e);
    } catch (final IllegalAccessException e) {
      LOGGER.warn("Ignoring invalid processor " + name, e);
    } catch (final ClassNotFoundException e) {
      LOGGER.warn("Ignoring invalid processor " + name, e);
    }
    return null;
  }

}
