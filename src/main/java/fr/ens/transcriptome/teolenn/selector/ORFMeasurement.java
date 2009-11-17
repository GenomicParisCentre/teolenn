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

package fr.ens.transcriptome.teolenn.selector;

import fr.ens.transcriptome.teolenn.TeolennException;

public class ORFMeasurement extends SimpleSelectorMeasurement {

  /** Measurement name. */
  public static final String MEASUREMENT_NAME = "orf";

  /**
   * Calc the measurement of a sequence.
   * @param chromosome the chromosome of sequence to use for the measurement
   * @param startPos the start position of sequence to use for the measurement
   * @param oligoLength the length of the oligonucleotide
   * @return an object as result
   */
  public Object calcMesurement(final String chromosome, final int startPos,
      final int oligoLength) {
    return null;
  }

  /**
   * Get the description of the measurement.
   * @return the description of the measurement
   */
  public String getDescription() {
    return "ORF";
  }

  /**
   * Get the name of the measurement.
   * @return the name of the measurement
   */
  public String getName() {
    return MEASUREMENT_NAME;
  }

  /**
   * Get the type of the result of calcMeasurement.
   * @return the type of the measurement
   */
  public Object getType() {
    return String.class;
  }

  /**
   * Run the initialization phase of the parameter.
   * @throws TeolennException if an error occurs while the initialization phase
   */
  public void init() throws TeolennException {
  }

  /**
   * Parse a string to an object return as calcMeasurement.
   * @param s String to parse
   * @return an object
   */
  public Object parse(final String s) {
    return null;
  }

  /**
   * Set a parameter for the filter.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  public void setInitParameter(final String key, final String value) {
  }

}
