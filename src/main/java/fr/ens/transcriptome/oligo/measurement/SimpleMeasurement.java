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

/**
 * This class define an abstract class for simple measurements.
 * @author Laurent Jourdren
 */
public abstract class SimpleMeasurement implements Measurement {

  /**
   * Get the score for the measurement.
   * @param value value
   * @return the score
   */
  public float getScore(final Object value) {

    return 0;
  }

  /**
   * Compute statistics of the measurement.
   * @return a Properties object with all statistics
   */
  public Properties computeStatistics() {

    return null;
  }

  public void clear() {
  }

  /**
   * Set a property of the measurement.
   * @param key key of the property to set
   * @param value value of the property to set
   */
  public void setProperty(final String key, final String value) {
  }

  /**
   * Add last measurements value to the statistics.
   */
  public void addLastMeasurementToStats() {
  }

}
