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

import fr.ens.transcriptome.teolenn.TeolennException;
import fr.ens.transcriptome.teolenn.measurement.ChromosomeMeasurement;
import fr.ens.transcriptome.teolenn.measurement.OligoStartMeasurement;
import fr.ens.transcriptome.teolenn.measurement.resource.OligoSequenceResource;
import fr.ens.transcriptome.teolenn.sequence.Sequence;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;
import fr.ens.transcriptome.teolenn.sequence.SequenceWriter;

/**
 * This class define a fasta measurement writer.
 * @author Laurent Jourdren
 */
public class FastaMeasurementWriter implements SequenceMeasurementsWriter {

  private boolean first = true;
  private int indexStart = -1;
  private int indexChromosome = -1;

  private SequenceWriter writer;
  private OligoSequenceResource fastaReader;
  private Sequence sequence = new Sequence();

  /**
   * Write a sequence measurement to the file.
   * @param sm Sequence measurement to write
   * @throws IOException if an error occurs while writing data
   */
  public void writeSequenceMesurement(final SequenceMeasurements sm)
      throws IOException {

    if (this.writer == null)
      return;

    if (first) {

      this.indexStart =
          sm.getIndexMeasurment(OligoStartMeasurement.MEASUREMENT_NAME);
      this.indexChromosome =
          sm.getIndexMeasurment(ChromosomeMeasurement.MEASUREMENT_NAME);

      if (this.indexStart == -1)
        throw new RuntimeException("Unable to find start measurement.");

      if (this.indexChromosome == -1)
        throw new RuntimeException("Unable to find chromosome measurement.");

      first = false;
    }

    final Object[] values = sm.getArrayMeasurementValues();

    if (values == null)
      throw new RuntimeException("Nothing to write.");

    this.sequence =
        this.fastaReader.getSequence((String) values[this.indexChromosome],
            (Integer) values[this.indexStart]);

    this.writer.write(sequence);
  }

  /**
   * Close the reader.
   * @throws IOException if an error occurs while closing the reader
   */
  public void close() throws IOException {

    this.fastaReader.close();
    this.writer.close();
  }

  //
  // Constructor
  //

  /**
   * Public constructor.
   * @param outputFile The output file
   * @param oligosDir The directory of the oligonucleotides
   * @param fastaExtension the extension of the fasta files
   * @param oligoLength The length of the oligonucleotides
   * @param start1 true if the fisrt position of the chromosome is 1
   */
  public FastaMeasurementWriter(final File outputFile, final File oligosDir,
      final String fastaExtension, final int oligoLength, final boolean start1)
      throws IOException, TeolennException {

    if (outputFile == null)
      throw new NullPointerException("File is null");

    this.writer = new SequenceWriter(outputFile);
    this.fastaReader = OligoSequenceResource.getRessource();

  }

}
