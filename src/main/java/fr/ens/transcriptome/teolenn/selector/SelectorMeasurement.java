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

import fr.ens.transcriptome.teolenn.measurement.Measurement;

public interface SelectorMeasurement extends Measurement {

  /**
   * Calc the measurement of a sequence.
   * @param chromosome the chromosome of sequence to use for the measurement
   * @param startPos the start position of sequence to use for the measurement
   * @return an object as result
   */
  Object calcMesurement(String chromosome, int startPos);

}
