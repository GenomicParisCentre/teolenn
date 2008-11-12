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

package fr.ens.transcriptome.teolenn.measurement;

import java.io.IOException;

import fr.ens.transcriptome.teolenn.Sequence;

/**
 * This class define a string measurement.
 * @author Laurent Jourdren
 */
public abstract class StringMeasurement extends SimpleMeasurement {

  /**
   * Calc the measurement of a sequence.
   * @param sequence the sequence to use for the measurement
   * @return an String Object
   */
  protected abstract String calcStringMeasurement(final Sequence sequence);

  /**
   * Calc the measurement of a sequence.
   * @param sequence the sequence to use for the measurement
   * @return an object as result
   */
  public Object calcMesurement(final Sequence sequence) {

    return calcStringMeasurement(sequence);
  }

  /**
   * Get the type of the result of calcMeasurement.
   * @return the type of the measurement
   */
  public Object getType() {

    return String.class;
  }

  /**
   * Parse a string to an object return as calcMeasurement.
   * @param s String to parse
   * @return an object
   */
  public Object parse(final String s) {

    return s;
  }

  /**
   * Set a parameter for the filter.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  public void setInitParameter(final String key, final String value) {
  }

  /**
   * Run the initialization phase of the parameter.
   */
  public void init() {
  }

}
