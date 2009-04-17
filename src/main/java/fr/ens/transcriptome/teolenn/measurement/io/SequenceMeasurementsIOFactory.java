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

import fr.ens.transcriptome.teolenn.Settings;

/**
 * This class define a factory to create SequenceMeasurementsReader or
 * SequenceMeasurementsWriter objects.
 * @author Laurent Jourdren
 */
public class SequenceMeasurementsIOFactory {

  /**
   * Create a new SequenceMeasurementsReader object.
   * @param file file to read
   * @return a new SequenceMeasurementsReader object.
   * @throws IOException if an error occurs while creating the reader.
   */
  public static final SequenceMeasurementsReader createSequenceMeasurementsReader(
      final File file) throws IOException {

    if (Settings.isMeasurementFileSerialized())
      return new SerializedSequenceMeasurementsReader(file);

    return new FileSequenceMeasurementsReader(file);
  }

  /**
   * Create a new SequenceMeasurementsWriter object.
   * @param file file to create
   * @return a new SequenceMeasurementsWriter object.
   * @throws IOException if an error occurs while creating the writer.
   */
  public static final SequenceMeasurementsWriter createSequenceMeasurementsWriter(
      final File file) throws IOException {

    if (Settings.isMeasurementFileSerialized())
      return new SerializedSequenceMeasurementsWriter(file);

    return new FileSequenceMeasurementsWriter(file);
  }

  /**
   * Create a new SequenceMeasurementsReader object for filtered files.
   * @param filteredFile file to read
   * @param originalFile non filtered file to read
   * @return a new SequenceMeasurementsReader object.
   * @throws IOException if an error occurs while creating the reader.
   */
  public static final SequenceMeasurementsReader createSequenceMeasurementsFilteredReader(
      final File filteredFile, final File originalFile) throws IOException {

    if (Settings.isMeasurementFileSerialized())
      return new FilteredSequenceMeasurementsReader(filteredFile,
          createSequenceMeasurementsReader(originalFile));

    return createSequenceMeasurementsReader(filteredFile);
  }

  /**
   * Create a new SequenceMeasurementsWriter object for filtered files.
   * @param filteredFile file to create
   * @param originalFile original file to filter
   * @return a new SequenceMeasurementsWriter object.
   * @throws IOException if an error occurs while creating the writer.
   */
  public static final SequenceMeasurementsWriter createSequenceMeasurementsFilteredWriter(
      final File filteredFile, final File originalFile) throws IOException {

    if (Settings.isMeasurementFileSerialized())
      return new FilteredSequenceMeasurementsWriter(filteredFile);

    return createSequenceMeasurementsWriter(filteredFile);
  }

  /**
   * Create a new SequenceMeasurementsWriter object for the select file.
   * @param file file to create
   * @return a new SequenceMeasurementsWriter object.
   * @throws IOException if an error occurs while creating the writer.
   */
  public static final SequenceMeasurementsWriter createSequenceMeasurementsSelectWriter(
      final File file) throws IOException {

    return new FileSequenceMeasurementsWriter(file);
  }

}
