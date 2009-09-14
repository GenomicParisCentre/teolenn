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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;

/**
 * This class define a container for SequenceMeasurementsWriter classes.
 * @author Laurent Jourdren
 */
public class MultiSequenceMeasurementWriter implements
    SequenceMeasurementsWriter {

  private List<SequenceMeasurementsWriter> writers =
      new ArrayList<SequenceMeasurementsWriter>();

  private SequenceMeasurementsWriter[] arrayWriter = null;

  /**
   * Add a writer.
   * @param writer Writer to add
   */
  public void addWriter(final SequenceMeasurementsWriter writer) {

    if (writer == null)
      return;

    this.writers.add(writer);
  }

  /**
   * Write a sequence measurement to the file.
   * @param sm Sequence measurement to write
   * @throws IOException if an error occurs while writing data
   */
  public void writeSequenceMesurement(final SequenceMeasurements sm)
      throws IOException {

    if (this.arrayWriter == null) {

      this.arrayWriter = new SequenceMeasurementsWriter[this.writers.size()];
      for (int i = 0; i < this.writers.size(); i++)
        this.arrayWriter[i] = this.writers.get(i);
    }

    for (int i = 0; i < this.arrayWriter.length; i++)
      this.arrayWriter[i].writeSequenceMesurement(sm);

  }

  /**
   * Close the reader.
   * @throws IOException if an error occurs while closing the reader
   */
  public void close() throws IOException {

    for (int i = 0; i < this.arrayWriter.length; i++)
      this.arrayWriter[i].close();

  }

  //
  // Constructor
  //

  /**
   * Public constructor.
   * @param writers Writer to add
   */
  public MultiSequenceMeasurementWriter(
      final SequenceMeasurementsWriter... writers) {

    for (SequenceMeasurementsWriter writer : writers)
      addWriter(writer);

  }

}
