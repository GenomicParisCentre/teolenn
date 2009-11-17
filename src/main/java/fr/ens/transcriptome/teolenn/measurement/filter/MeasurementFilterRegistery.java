/*
 *                  Teolenn development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU General Public License version 2 or later. This
 * should be distributed with the code. If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/licenses/gpl-2.0.txt
 *
 * Copyright for this code is held jointly by the microarray platform
 * of the École Normale Supérieure and the individual authors.
 * These should be listed in @author doc comments.
 *
 * For more information on the Teolenn project and its aims,
 * or to join the Teolenn Google group, visit the home page
 * at:
 *
 *      http://www.transcriptome.ens.fr/teolenn
 *
 */

package fr.ens.transcriptome.teolenn.measurement.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import fr.ens.transcriptome.teolenn.Globals;
import fr.ens.transcriptome.teolenn.measurement.MeasurementRegistery;

/**
 * This class define a registery for measurement filters
 * @author Laurent Jourdren
 */
public class MeasurementFilterRegistery {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  private static Map<String, Class> registery = new HashMap<String, Class>();

  /**
   * Add a measurement filter
   * @param name name of the measurement filter
   * @param clazz Class of the sequence filter
   */
  public static void addMeasurementFilterType(final String name,
      final Class clazz) {

    if (name == null || clazz == null)
      return;

    final String lowerName = name.toLowerCase();

    if (registery.containsKey(lowerName))
      logger.warning("Measurement filter "
          + name + " already exits, override previous measurement filter.");

    registery.put(lowerName, clazz);
  }

  /**
   * Add a measurement filter
   * @param name name of the measurement filter
   * @param className Name of of the measurement filter
   */
  public static void addMeasurementFilterType(final String name,
      final String className) {

    if (name == null || "".equals(name) || className == null)
      return;

    try {
      Class clazz = MeasurementRegistery.class.forName(className);

      addMeasurementFilterType(name.toLowerCase(), clazz);

      logger.info("Add external measurement: " + name);

    } catch (ClassNotFoundException e) {

      logger.severe("Cannot find "
          + className + " for " + name + " measurement filter addon");
      throw new RuntimeException("Cannot find "
          + className + " for " + name + " measurement filter addon");

    }
  }

  private static boolean testClassType(final Class clazz) {

    if (clazz == null)
      return false;

    try {
      return clazz.newInstance() instanceof MeasurementFilter;
    } catch (InstantiationException e) {
      logger.severe("Can't create instance of "
          + clazz.getName()
          + ". Maybe your class doesn't have a void constructor.");
    } catch (IllegalAccessException e) {
      logger.severe("Can't access to " + clazz.getName());
    }

    return false;
  }

  /**
   * Get a new instance of a measurement filter from its name.
   * @param name The name of the measurement filter to get
   * @return a new instance of a measurement filter or null if the requested
   *         measurement doesn't exists
   */
  public static MeasurementFilter getMeasuremrentFilter(final String name) {

    if (name == null)
      return null;

    Class clazz = registery.get(name.toLowerCase());

    if (clazz == null)
      return null;

    try {

      return (MeasurementFilter) clazz.newInstance();

    } catch (InstantiationException e) {
      System.err.println("Unable to instantiate "
          + name
          + " filter. Maybe this filter doesn't have a void constructor.");
      logger.severe("Unable to instantiate "
          + name
          + " filter. Maybe this filter doesn't have a void constructor.");
      return null;
    } catch (IllegalAccessException e) {
      return null;
    }

  }

  //
  // Static Constructor
  //

  static {

    addMeasurementFilterType(FloatRangeFilter.MEASUREMENT_FILTER_NAME,
        FloatRangeFilter.class);
    addMeasurementFilterType(BooleanFilter.MEASUREMENT_FILTER_NAME,
        BooleanFilter.class);
    addMeasurementFilterType(ORFsFilter.MEASUREMENT_FILTER_NAME,
        ORFsFilter.class);
  }

}
