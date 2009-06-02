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

import java.util.Properties;

import fr.ens.transcriptome.teolenn.Module;
import fr.ens.transcriptome.teolenn.sequence.Sequence;

/**
 * This interface define a measurement.
 * @author Laurent Jourdren
 */
public interface Measurement extends Module {

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
   * Calc the measurement of a sequence.
   * @param sequence the sequence to use for the measurement
   * @return an object as result
   */
  Object calcMesurement(Sequence sequence);

  /**
   * Get the score for the measurement.
   * @param value value
   * @return the score
   */
  float getScore(Object value);

  /**
   * Compute statistics of the measurement.
   * @return a Properties object with all statistics
   */
  Properties computeStatistics();

  /**
   * Set a property of the measurement.
   * @param key key of the property to set
   * @param value value of the property to set
   */
  void setProperty(String key, String value);

  /**
   * Clear the results and the current statistics.
   */
  void clear();

  /**
   * Add last measurements value to the statistics.
   */
  void addLastMeasurementToStats();

}
