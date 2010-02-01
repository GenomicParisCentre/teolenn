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

package fr.ens.transcriptome.teolenn.output;

import java.io.File;
import java.io.IOException;
import java.util.List;

import fr.ens.transcriptome.teolenn.DesignConstants;
import fr.ens.transcriptome.teolenn.TeolennException;
import fr.ens.transcriptome.teolenn.measurement.ChromosomeMeasurement;
import fr.ens.transcriptome.teolenn.measurement.OligoLengthMeasurement;
import fr.ens.transcriptome.teolenn.measurement.OligoStartMeasurement;
import fr.ens.transcriptome.teolenn.resource.OligoSequenceResource;
import fr.ens.transcriptome.teolenn.resource.ORFResource.ORF;
import fr.ens.transcriptome.teolenn.selector.ORFMeasurement;
import fr.ens.transcriptome.teolenn.sequence.Sequence;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;
import fr.ens.transcriptome.teolenn.sequence.SequenceWriter;
import fr.ens.transcriptome.teolenn.util.StringUtils;

/**
 * This class define a fasta measurement writer.
 * @author Laurent Jourdren
 */
public class FastaOutput implements Output {

  /** Output name. */
  public static final String OUTPUT_NAME = "fasta";

  private boolean first = true;
  private int indexStart = -1;
  private int indexChromosome = -1;
  private int indexORF = -1;
  private int indexLength = -1;

  private File outputFile;
  private String outputDefaultFile;
  private boolean rcORF = true;
  private boolean rc12;

  private SequenceWriter writer;
  private OligoSequenceResource fastaReader;
  private Sequence sequence = new Sequence();
  private boolean reverseNextSequence;

  /**
   * Get the description of the output.
   * @return the description of the output
   */
  public String getDescription() {

    return "Write SequenceMeasurement to Fasta format";
  }

  /**
   * Get the name of the output.
   * @return the name of the output
   */
  public String getName() {

    return OUTPUT_NAME;
  }

  /**
   * Run the initialization phase of the output.
   * @throws TeolennException if an error occurs while the initialization phase
   */
  public void init() throws TeolennException {

    if (this.outputFile == null)
      this.outputFile =
          new File(StringUtils.basename(this.outputDefaultFile) + ".fasta");

    try {
      this.writer = new SequenceWriter(outputFile);

    } catch (IOException e) {
      throw new TeolennException(e);
    }

    this.fastaReader = OligoSequenceResource.getRessource();
  }

  /**
   * Set a parameter for the output.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  public void setInitParameter(String key, String value) {

    if (key == null || value == null)
      return;
    if (DesignConstants.OUTPUT_DEFAULT_FILE_PARAMETER_NAME.equals(key))
      this.outputDefaultFile = value;

    if ("outputfile".equals(key))
      this.outputFile = new File(value);

    if ("rcorf".equals(key))
      this.rcORF = Boolean.parseBoolean(value);

    if ("rc12".equals(key))
      this.rc12 = Boolean.parseBoolean(value);
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

    if (first) {

      this.indexStart =
          sm.getIndexMeasurment(OligoStartMeasurement.MEASUREMENT_NAME);
      this.indexChromosome =
          sm.getIndexMeasurment(ChromosomeMeasurement.MEASUREMENT_NAME);
      this.indexORF = sm.getIndexMeasurment(ORFMeasurement.MEASUREMENT_NAME);
      this.indexLength =
          sm.getIndexMeasurment(OligoLengthMeasurement.MEASUREMENT_NAME);

      if (this.indexStart == -1)
        throw new RuntimeException("Unable to find start measurement.");

      if (this.indexChromosome == -1)
        throw new RuntimeException("Unable to find chromosome measurement.");

      if (this.indexLength == -1)
        throw new RuntimeException("Unable to find oligo length measurement.");

      first = false;
    }

    final Object[] values = sm.getArrayMeasurementValues();

    if (values == null)
      throw new RuntimeException("Nothing to write.");

    final String chr = (String) values[this.indexChromosome];
    final int start = (Integer) values[this.indexStart];
    final int length = (Integer) values[this.indexLength];

    try {
      this.sequence = this.fastaReader.getSequence(chr, start, length);
    } catch (TeolennException e) {
      throw new IOException("Unable to get the length of chromosome before "
          + "retrieve oligonucleotide sequence");
    }

    boolean reverse = false;

    if (this.rcORF && this.indexORF != -1)
      if (!((List<ORF>) values[this.indexORF]).get(0).codingStrand)
        reverse = true;

    if (this.rc12) {

      if (this.reverseNextSequence) {

        reverse = true;

        this.reverseNextSequence = false;
      } else
        this.reverseNextSequence = true;
    }

    if (reverse)
      sequence.reverseComplementSequence();

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
   * @throws TeolennException
   */
  public FastaOutput() throws TeolennException {
  }

}
