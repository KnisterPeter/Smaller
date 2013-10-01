package de.matrixweb.smaller.resource;

/**
 * @author markusw
 */
public interface ProcessorFactory {

  /**
   * This methods returns the latest available version of a processor if
   * possible.
   * 
   * @param name
   *          The name of the {@link Processor} to create
   * @return Returns an instance of {@link Processor}
   */
  Processor getProcessor(String name);

  /**
   * @param name
   *          The name of the {@link Processor} to create
   * @param version
   *          The version of the named {@link Processor}
   * @return Returns an instance of {@link Processor}
   */
  Processor getProcessor(String name, String version);

  /**
   * Disposes the factory.
   */
  void dispose();

}
