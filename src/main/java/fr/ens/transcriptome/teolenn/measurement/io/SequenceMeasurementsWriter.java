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

package fr.ens.transcriptome.teolenn.measurement.io;

import java.io.IOException;

import fr.ens.transcriptome.teolenn.SequenceMeasurements;

/**
 * This class define a sequence measurement writer.
 * @author Laurent Jourdren
 */
public interface SequenceMeasurementsWriter {

  /**
   * Write a sequence measurement to the file.
   * @param sm Sequence measurement to write
   * @throws IOException if an error occurs while writing data
   */
  void writeSequenceMesurement(final SequenceMeasurements sm)
      throws IOException;

  /**
   * Close the writer.
   * @throws IOException if an error occurs while closing the writer
   */
  public void close() throws IOException;

}
