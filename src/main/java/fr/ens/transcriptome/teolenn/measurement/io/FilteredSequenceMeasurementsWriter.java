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
import java.io.ObjectOutputStream;

import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;
import fr.ens.transcriptome.teolenn.util.FileUtils;

/**
 * This class define a writer for SequenceMeasurement that only store the id of
 * measurements using serialization.
 * @author Laurent Jourdren
 */
public class FilteredSequenceMeasurementsWriter implements
    SequenceMeasurementsWriter {

  // private static Logger logger = Logger.getLogger(Globals.APP_NAME);
  private static final String SERIALIZED_FORMAT_VERSION =
      "TEOLENN__FILTERED_MES_1";

  private ObjectOutputStream out;

  private boolean headerDone;

  /**
   * Write the header of the file.
   * @param sm the object that contains the name and type of measurements
   * @throws IOException if an error occurs while writing header
   */
  private void writeHeader(final SequenceMeasurements sm) throws IOException {

    if (headerDone)
      return;

    // Write MAGIC COOKIE
    out.writeUTF(SERIALIZED_FORMAT_VERSION);

    this.headerDone = true;
  }

  /**
   * Write a sequence measurement to the file.
   * @param sm Sequence measurement to write
   * @throws IOException if an error occurs while writing data
   */
  public void writeSequenceMesurement(final SequenceMeasurements sm)
      throws IOException {

    if (this.out == null)
      return;

    // Write the header
    if (!headerDone)
      writeHeader(sm);

    // Write the id of the current measurement
    final int id = sm.getId();
    out.writeInt(id);

    if ((id % 100000 == 0))
      out.reset();
  }

  /**
   * Close the writer.
   * @throws IOException if an error occurs while closing the writer
   */
  public void close() throws IOException {

    this.out.close();
    this.out = null;
  }

  //
  // Constructor
  //

  /**
   * Public constructor.
   * @param outputFile file to write
   */
  public FilteredSequenceMeasurementsWriter(final File outputFile)
      throws IOException {

    if (outputFile == null)
      throw new NullPointerException("File is null");

    this.out = FileUtils.createObjectOutputWriter(outputFile);
  }

}
