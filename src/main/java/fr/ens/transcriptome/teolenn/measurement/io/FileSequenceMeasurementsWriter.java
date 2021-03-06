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

package fr.ens.transcriptome.teolenn.measurement.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;

import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;
import fr.ens.transcriptome.teolenn.util.FileUtils;

/**
 * This class define a sequence measurement writer based on simple text file.
 * @author Laurent Jourdren
 */
public final class FileSequenceMeasurementsWriter implements
    SequenceMeasurementsWriter {

  private static final int WRITE_BUFFER_LEN = 1000000;

  private Writer writer;
  private final StringBuilder buffer =
      new StringBuilder(WRITE_BUFFER_LEN + 50000);
  private boolean headerDone;

  private void writeHeader(SequenceMeasurements sm) throws IOException {

    if (headerDone)
      return;

    buffer.append("Id");

    final String[] names = sm.getArrayMesurementNames();

    for (int i = 0; i < names.length; i++) {

      buffer.append("\t");
      buffer.append(names[i]);
    }
    buffer.append("\n");

    writer.append(buffer.toString());
    buffer.setLength(0);

    this.headerDone = true;
  }

  /**
   * Write a sequence measurement to the file.
   * @param sm Sequence measurement to write
   * @throws IOException if an error occurs while writing data
   */
  public void writeSequenceMesurement(final SequenceMeasurements sm)
      throws IOException {

    if (this.writer == null)
      return;

    if (!headerDone)
      writeHeader(sm);

    buffer.append(sm.getId());

    final Object[] values = sm.getArrayMeasurementValues();

    for (int i = 0; i < values.length; i++) {

      buffer.append("\t");
      buffer.append(values[i]);
    }
    buffer.append("\n");

    if (buffer.length() > WRITE_BUFFER_LEN) {
      writer.append(buffer.toString());
      buffer.setLength(0);
    }
  }

  /**
   * Close the writer.
   * @throws IOException if an error occurs while closing the writer
   */
  public void close() throws IOException {

    writer.append(buffer.toString());
    this.writer.close();
    this.writer = null;
  }

  //
  // Constructor
  //

  /**
   * Public constructor.
   * @param file file to write
   */
  public FileSequenceMeasurementsWriter(final File file)
      throws FileNotFoundException {

    if (file == null)
      throw new NullPointerException("File is null");

    this.writer = FileUtils.createBufferedWriter(file);
  }

}
