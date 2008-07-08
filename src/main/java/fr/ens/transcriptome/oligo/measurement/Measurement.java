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

import java.util.Properties;

import fr.ens.transcriptome.oligo.Sequence;

public interface Measurement {

  /**
   * Get the name of the measurement.
   * @return the name of the measurement
   */
  String getName();

  /**
   * Get the description of the measurement.
   * @return the description of the measurement
   */
  String getDescription();

  /**
   * Get the type of the result of calcMeasurement.
   * @return the type of the measurement
   */
  Object getType();

  /**
   * Parse a string to an object return as calcMeasurement.
   * @param s String to parse
   * @return an object
   */
  Object parse(String s);

  /**
   * Calc the measurement of a sequence .
   * @param sequence the sequence to use for the measurement
   * @return an object as result
   */
  Object calcMesurement(Sequence sequence);

  float getScore(Object value);

  Properties computeStatistics();

  void setProperty(String key, String value);

  void clear();

  void addLastMeasurementToStats();

}
