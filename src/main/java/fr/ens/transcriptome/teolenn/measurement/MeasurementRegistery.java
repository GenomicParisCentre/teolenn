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
 * or to join the Teolenn mailing list, visit the home page
 * at:
 *
 *      http://www.transcriptome.ens.fr/teolenn
 *
 */

package fr.ens.transcriptome.teolenn.measurement;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import fr.ens.transcriptome.teolenn.Globals;

/**
 * This class define a registery that contains the list of available
 * measurements.
 * @author Laurent Jourdren
 */
public class MeasurementRegistery {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  private static Map<String, Class> registery = new HashMap<String, Class>();

  /**
   * Add a measurement
   * @param name name of the measurement
   * @param clazz Class of the measurement
   */
  public static void addMeasurementType(final String name, final Class clazz) {

    if (name == null || clazz == null)
      return;

    if (testClassType(clazz))
      registery.put(name.toLowerCase(), clazz);
    else
      logger.warning("Addon " + name + " is not a measurement class");
  }

  /**
   * Add a measurement
   * @param name name of the measurement
   * @param className Name of of the measurement
   */
  public static void addMeasurementType(final String name,
      final String className) {

    if (name == null || "".equals(name) || className == null)
      return;

    try {
      Class clazz = MeasurementRegistery.class.forName(className);

      addMeasurementType(name, clazz);

      logger.info("Add external measurement: " + name);

    } catch (ClassNotFoundException e) {

      logger.severe("Cannot find "
          + className + " for " + name + " measurement addon");
      throw new RuntimeException("Cannot find "
          + className + " for " + name + " measurement addon");

    }
  }

  private static boolean testClassType(final Class clazz) {

    if (clazz == null)
      return false;

    try {
      return clazz.newInstance() instanceof Measurement;
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
   * Get a new instance of a measurement from its name.
   * @param name The name of the measurement to get
   * @return a new instance of a measurement or null if the requested
   *         measurement doesn't exists
   */
  public static Measurement getMeasurement(final String name) {

    if (name == null)
      return null;

    Class clazz = registery.get(name.toLowerCase());

    if (clazz == null)
      return null;

    try {

      return (Measurement) clazz.newInstance();

    } catch (InstantiationException e) {

      return null;
    } catch (IllegalAccessException e) {

      return null;
    }

  }

  //
  // Static Constructor
  //

  static {

    addMeasurementType("scaffold", ScaffoldMeasurement.class);
    addMeasurementType("start", OligoStartMeasurement.class);
    addMeasurementType("position", PositionMeasurement.class);
    addMeasurementType("tm", TmMeasurement.class);
    addMeasurementType("%gc", GCPencentMeasurement.class);
    addMeasurementType("complexity", ComplexityMeasurement.class);
    addMeasurementType("unicity", UnicityMeasurement.class);
    addMeasurementType("oligosequence", OligoSequenceMeasurement.class);

  }

}
