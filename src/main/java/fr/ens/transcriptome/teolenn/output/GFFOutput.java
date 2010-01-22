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
import java.io.Writer;

import fr.ens.transcriptome.teolenn.DesignConstants;
import fr.ens.transcriptome.teolenn.Globals;
import fr.ens.transcriptome.teolenn.TeolennException;
import fr.ens.transcriptome.teolenn.measurement.ChromosomeMeasurement;
import fr.ens.transcriptome.teolenn.measurement.OligoLengthMeasurement;
import fr.ens.transcriptome.teolenn.measurement.OligoStartMeasurement;
import fr.ens.transcriptome.teolenn.selector.GlobalScoreMeasurement;
import fr.ens.transcriptome.teolenn.selector.ORFMeasurement;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;
import fr.ens.transcriptome.teolenn.util.FileUtils;
import fr.ens.transcriptome.teolenn.util.StringUtils;

/**
 * This class define a gff measurement writer.
 * @author Laurent Jourdren
 */
public class GFFOutput implements Output {

  private static final String SEQID_PREFIX = "T_";
  private static final String SOURCE = Globals.APP_NAME_LOWER_CASE;
  private static final String TYPE = "oligo";

  private boolean first = true;
  private int indexStart = -1;
  private int indexChromosome = -1;
  private int indexLength = -1;
  private int indexScore = -1;
  private int indexORF = -1;

  private Writer writer;

  private File outputFile;
  private File outputDir;
  private String outputDefaultFile;

  private int count = 0;
  private final StringBuilder sb = new StringBuilder();

  /** Output name. */
  public static final String OUTPUT_NAME = "gff";

  /**
   * Get the description of the output.
   * @return the description of the output
   */
  public String getDescription() {

    return "Write SequenceMeasurement to GFF format";
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
          new File(StringUtils.basename(this.outputDefaultFile) + ".gff");

    try {
      this.writer = FileUtils.createBufferedWriter(outputFile);
      this.writer.write("##gff-version 3\n");
    } catch (IOException e) {
      throw new TeolennException(e);
    }

  }

  /**
   * Set a parameter for the output.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  public void setInitParameter(String key, String value) {

    if (key == null || value == null)
      return;
    if (DesignConstants.OUTPUT_DIR_PARAMETER_NAME.equals(key))
      this.outputDir = new File(value);
    if (DesignConstants.OUTPUT_DEFAULT_FILE_PARAMETER_NAME.equals(key))
      this.outputDefaultFile = value;

    if ("outputfile".equals(key))
      this.outputFile = new File(value);

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
      this.indexLength =
          sm.getIndexMeasurment(OligoLengthMeasurement.MEASUREMENT_NAME);
      this.indexScore =
          sm.getIndexMeasurment(GlobalScoreMeasurement.MEASUREMENT_NAME);
      this.indexORF = sm.getIndexMeasurment(ORFMeasurement.MEASUREMENT_NAME);

      if (this.indexStart == -1)
        throw new RuntimeException("Unable to find start measurement.");

      if (this.indexChromosome == -1)
        throw new RuntimeException("Unable to find chromosome measurement.");

      first = false;
    }

    final Object[] values = sm.getArrayMeasurementValues();

    if (values == null)
      throw new RuntimeException("Nothing to write.");

    final int start = (Integer) values[this.indexStart];
    final int length = (Integer) values[this.indexLength];
    final String chr = (String) values[this.indexChromosome];
    final float score = (Float) values[this.indexScore];
    final String orf = (String) values[this.indexORF];
    final char strand = orf.endsWith("W") ? '+' : '-';
    final String phase = ".";

    // column 1: seqid
    sb.append(chr);
    sb.append('\t');

    // column 2: source
    sb.append(SOURCE);
    sb.append('\t');

    // column 3: type
    sb.append("oligo");
    sb.append('\t');

    // column 4: start
    sb.append(start);
    sb.append('\t');

    // column 5: end
    sb.append(start + length);
    sb.append('\t');

    // column 6: score
    sb.append(score);
    sb.append('\t');

    // column 7: strand
    sb.append(strand);
    sb.append('\t');

    // column 8: phase
    sb.append(phase);
    sb.append('\t');

    // column 9: attributes
    sb.append("ID=");
    sb.append(sm.getId());

    // eof
    sb.append('\n');

    this.writer.write(sb.toString());
    sb.setLength(0);

  }

  /**
   * Close the reader.
   * @throws IOException if an error occurs while closing the reader
   */
  public void close() throws IOException {

    System.out.println("Close.");
    this.writer.close();
  }

  //
  // Constructor
  //

  /**
   * Public constructor.
   * @throws TeolennException
   */
  public GFFOutput() throws TeolennException {
  }
}
