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

import fr.ens.transcriptome.teolenn.Sequence;

/**
 * This class define a measurent that returns the position start of the
 * sequences.
 * @author Laurent Jourdren
 */
public final class OligoStartMeasurement extends IntegerMeasurement {

  /** Measurement name. */
  public static final String MEASUREMENT_NAME = "OligoStart";

  /**
   * Calc the measurement of a sequence.
   * @param sequence the sequence to use for the measurement
   * @return an int value
   */
  protected int calcIntMeasurement(final Sequence sequence) {

    String seqName = sequence.getName();

    int startPos = seqName.indexOf(":subseq(");
    int endPos = seqName.indexOf(",", startPos);

    return Integer.parseInt(seqName.substring(startPos + 8, endPos));
  }

  /**
   * Get the description of the measurement.
   * @return the description of the measurement
   */
  public String getDescription() {

    return "Get the start position of the sequence";
  }

  /**
   * Get the name of the measurement.
   * @return the name of the measurement
   */
  public String getName() {

    return MEASUREMENT_NAME;
  }

}
