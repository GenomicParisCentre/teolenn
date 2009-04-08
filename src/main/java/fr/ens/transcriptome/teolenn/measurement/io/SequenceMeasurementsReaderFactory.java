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

package fr.ens.transcriptome.teolenn.measurement.io;

import java.io.File;
import java.io.IOException;

/**
 * This class define a factory to create SequenceMeasurementsReader objects.
 * @author Laurent Jourdren
 */
public final class SequenceMeasurementsReaderFactory {

  /**
   * Create a new SequenceMeasurementsReader object.
   * @param file file to read
   * @return a new SequenceMeasurementsReader object.
   * @throws IOException if an error occurs while creating the reader.
   */
  public static final SequenceMeasurementsReader createSequenceMeasurementsReader(
      final File file) throws IOException {

    return new FileSequenceMeasurementsReader(file);
  }

}
