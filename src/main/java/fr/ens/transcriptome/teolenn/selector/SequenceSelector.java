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

import fr.ens.transcriptome.teolenn.Module;
import fr.ens.transcriptome.teolenn.TeolennException;
import fr.ens.transcriptome.teolenn.WeightsSetter;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsReader;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsWriter;

/**
 * This interface define a Sequence selector.
 * @author Laurent Jourdren
 */
public interface SequenceSelector extends Module {

  /**
   * Proceed to the oligo selection.
   * @param measurementReader SequenceMeasurement reader
   * @param measurementWriter SequenceMeasurement writer
   * @param weightsSetters Weight to apply on measurements
   * @throws TeolennException if an error occurs while selecting sequences
   */
  void select(SequenceMeasurementsReader measurementReader,
      SequenceMeasurementsWriter measurementWriter, WeightsSetter weightsSetters)
      throws TeolennException;

}
