/*
 *                      Nividic development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the microarray platform
 * of the École Normale Supérieure and the individual authors.
 * These should be listed in @author doc comments.
 *
 * For more information on the Nividic project and its aims,
 * or to join the Nividic mailing list, visit the home page
 * at:
 *
 *      http://www.transcriptome.ens.fr/nividic
 *
 */

package fr.ens.transcriptome.oligo.measurement;

import java.util.HashMap;
import java.util.Map;

public class MeasurementRegistery {

  private static Map<String, Class> registery = new HashMap<String, Class>();

  public static void addMeasurementType(final String name, final Class clazz) {

    if (name == null || clazz == null)
      return;

    registery.put(name.toLowerCase(), clazz);
  }

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
  }

}
