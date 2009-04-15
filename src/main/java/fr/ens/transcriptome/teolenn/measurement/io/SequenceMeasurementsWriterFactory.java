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

import java.io.File;
import java.io.IOException;

/**
 * This class define a factory to create SequenceMeasurementsWriter objects.
 * @author Laurent Jourdren
 */
public class SequenceMeasurementsWriterFactory {

  /**
   * Create a new SequenceMeasurementsWriter object.
   * @param file file to create
   * @return a new SequenceMeasurementsWriter object.
   * @throws IOException if an error occurs while creating the writer.
   */
  public static final SequenceMeasurementsWriter createSequenceMeasurementsWriter(
      final File file) throws IOException {

    return new FileSequenceMeasurementsWriter(file);
    //return new SerializedSequenceMeasurementsWriter(file);
  }

}
