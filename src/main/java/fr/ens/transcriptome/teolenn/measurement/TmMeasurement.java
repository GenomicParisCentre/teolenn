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

package fr.ens.transcriptome.teolenn.measurement;

import fr.ens.transcriptome.teolenn.sequence.MeltingTemp;
import fr.ens.transcriptome.teolenn.sequence.Sequence;

/**
 * This class define a measurement that returns the Tm of the sequences.
 * @author Laurent Jourdren
 */
public class TmMeasurement extends FloatMeasurement {

  /** Measurement name. */
  public static final String MEASUREMENT_NAME = "Tm";

  /**
   * Calc the measurement of a sequence.
   * @param sequence the sequence to use for the measurement
   * @return a float value
   */
  public float calcFloatMeasurement(Sequence sequence) {

    return MeltingTemp.tmstalucDNA(sequence.getSequence(), 50, 50);
  }

  /**
   * Get the name of the measurement.
   * @return the name of the measurement
   */
  public String getName() {

    return MEASUREMENT_NAME;
  }

  /**
   * Get the description of the measurement.
   * @return the description of the measurement
   */
  public String getDescription() {

    return "Calc Tm of sequence";
  }

  //
  // Constructor
  //

  /**
   * Public constructor.
   */
  public TmMeasurement() {

    super(0, 100);
  }

}
