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

package fr.ens.transcriptome.teolenn.selector;

import java.io.IOException;

import fr.ens.transcriptome.teolenn.TeolennException;
import fr.ens.transcriptome.teolenn.WeightsSetter;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsReader;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsWriter;

/**
 * This interface define a Sequence selector.
 * @author Laurent Jourdren
 */
public interface SequenceSelector {

  /**
   * Set a parameter for the filter.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  void setInitParameter(String key, String value);

  /**
   * Run the initialization phase of the parameter.
   * @throws IOException if an error occurs while the initialization phase
   */
  void init() throws IOException;

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
