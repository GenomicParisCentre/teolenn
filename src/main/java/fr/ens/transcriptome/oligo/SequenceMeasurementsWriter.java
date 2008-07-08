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

package fr.ens.transcriptome.oligo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;

public class SequenceMeasurementsWriter {

  // private static final int BUFFER_SIZE = 500000;
  // private static final int BUFFER_MAX_BEFORE_WRITING = BUFFER_SIZE - 10000;

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

  public void close() throws IOException {

    writer.append(buffer.toString());
    this.writer.close();
    this.writer = null;
  }

  //
  // Constructor
  //

  public SequenceMeasurementsWriter(final File file)
      throws FileNotFoundException {

    if (file == null)
      throw new NullPointerException("File is null");

    this.writer = Util.createBufferedWriter(file);
  }
}
