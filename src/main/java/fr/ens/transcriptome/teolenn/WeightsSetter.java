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

package fr.ens.transcriptome.teolenn;

import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;

/**
 * This abstract class allow the user to set weights and other parameters
 * before selection.
 * @author Laurent Jourdren
 */
public abstract class WeightsSetter {

  /**
   * Define the weigths to use for selection
   * @param sm SequenceMeasurement that contains all the measurements to use
   *          by the selection algorithm.
   */
  public abstract void setWeights(final SequenceMeasurements sm);
}