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

package fr.ens.transcriptome.teolenn.output;

import java.io.File;
import java.io.IOException;

import fr.ens.transcriptome.teolenn.DesignConstants;
import fr.ens.transcriptome.teolenn.TeolennException;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsIOFactory;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsWriter;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;

public class DefaultOutput implements Output {

  private SequenceMeasurementsWriter writer;
  private File outputFile;
  private String outputDefaultFile;

  /** Output name. */
  public static final String OUTPUT_NAME = "default";

  /**
   * Get the description of the output.
   * @return the description of the output
   */
  public String getDescription() {

    return "Write SequenceMeasurement in default format";
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
      this.outputFile = new File(this.outputDefaultFile);

    try {
      this.writer =
          SequenceMeasurementsIOFactory
              .createSequenceMeasurementsSelectWriter(this.outputFile);
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

    this.writer.writeSequenceMesurement(sm);
  }

  /**
   * Close the writer.
   * @throws IOException if an error occurs while closing the writer
   */
  public void close() throws IOException {

    this.writer.close();
  }

}
