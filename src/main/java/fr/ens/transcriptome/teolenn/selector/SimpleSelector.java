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

package fr.ens.transcriptome.teolenn.selector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import fr.ens.transcriptome.teolenn.DesignConstants;
import fr.ens.transcriptome.teolenn.Globals;
import fr.ens.transcriptome.teolenn.TeolennException;
import fr.ens.transcriptome.teolenn.WeightsSetter;
import fr.ens.transcriptome.teolenn.measurement.Measurement;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsReader;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsWriter;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurementsStatReader;

public abstract class SimpleSelector implements SequenceSelector {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  private List<SelectorMeasurement> ms = new ArrayList<SelectorMeasurement>();

  private SequenceMeasurementsReader measurementReader;
  private SequenceMeasurementsWriter measurementWriter;
  private File statsFile;
  private SequenceMeasurements smReaded;
  private SequenceMeasurements smWrited;
  private int smReadedLength;
  private Object[] smReadedValues;
  private WeightsSetter weightsSetters;
  private int indexScaffold = -1;
  private int indexStartPosition = -1;
  private int indexFirstSelectorMeasurement = 0;

  private Object[] valuesToWrite;

  private boolean first = true;

  public abstract void doSelection() throws IOException;

  /**
   * Set a parameter for the filter.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  public void setInitParameter(final String key, final String value) {

    if (DesignConstants.STATS_FILE_PARAMETER_NAME.equals(key))
      this.statsFile = new File(value);
  }

  /**
   * Add a selector measurement.
   * @param measurement Measurement to add
   */
  public void addMeasurement(final SelectorMeasurement measurement) {

    if (measurement != null)
      ms.add(measurement);
  }

  /**
   * Proceed to the oligo selection.
   * @param measurementReader SequenceMeasurement reader
   * @param measurementWriter SequenceMeasurement writer
   * @param weightsSetters Weight to apply on measurements
   * @throws TeolennException if an error occurs while selecting sequences
   */
  public void select(final SequenceMeasurementsReader measurementReader,
      final SequenceMeasurementsWriter measurementWriter,
      final WeightsSetter weightsSetters) throws TeolennException {

    if (measurementReader == null)
      throw new TeolennException("No measurement reader defined for selector");

    if (measurementWriter == null)
      throw new TeolennException("No measurement writer defined for selector");

    if (weightsSetters == null)
      throw new TeolennException("No weights defined for selector");

    this.measurementReader = measurementReader;
    this.measurementWriter = measurementWriter;
    this.weightsSetters = weightsSetters;

    try {
      doSelection();
    } catch (IOException e) {

      throw new TeolennException(e);
    }
  }

  /**
   * Get the next SequenceMeasurements from the input SequenceMeasurements file.
   * @return a SequenceMeasurements. It's always the same object.
   * @throws IOException if an error occurs while reading data
   */
  public SequenceMeasurements next() throws IOException {

    this.smReaded = this.measurementReader.next(this.smReaded);
    final SequenceMeasurements smReaded = this.smReaded;

    if (smReaded == null)
      return null;

    if (first) {

      this.smReadedValues = smReaded.getArrayMeasurementValues();
      this.smReadedLength = smReaded.size();

      this.indexScaffold = smReaded.getIndexMeasurment("chromosome");
      this.indexStartPosition = smReaded.getIndexMeasurment("oligostart");

      this.smWrited = new SequenceMeasurements();

      // Add measurement field in output file
      for (Measurement m : smReaded.getMeasurements()) {
        this.smWrited.addMesurement(m);
        this.indexFirstSelectorMeasurement++;
      }

      for (SelectorMeasurement m : this.ms)
        this.smWrited.addMesurement(m);

      // Add Global score to the output file
      this.smWrited.addMesurement(new GlobalScoreMeasurement());

      this.valuesToWrite = new Object[smReadedLength + this.ms.size() + 1];
      this.smWrited.setArrayMeasurementValues(this.valuesToWrite);

      // Read stats
      SequenceMeasurementsStatReader smsr =
          new SequenceMeasurementsStatReader(this.statsFile, smReaded);
      smsr.read();

      // Set the weights
      weightsSetters.setWeights(smWrited);

      if (smWrited.isSumOfWeightEquals1())
        logger.warning("Sum of weights is not equals to 1.");

      first = false;
    }

    // Get the chromosome and the start position
    final String chromosome = (String) this.smReadedValues[indexScaffold];
    final int startPos = (Integer) this.smReadedValues[indexStartPosition];

    // Copy the read values
    System.arraycopy(this.smReadedValues, 0, this.valuesToWrite, 0,
        this.smReadedLength);

    // Add the values of SelectorMeasurements
    int i = this.smReadedLength;
    for (SelectorMeasurement m : this.ms)
      this.valuesToWrite[i++] = m.calcMesurement(chromosome, startPos);

    this.valuesToWrite[i++] = this.smWrited.getScore();
    this.smWrited.setId(smReaded.getId());

    return this.smWrited;
  }

  /**
   * Write a selected SequenceMeasurements.
   * @param sm SequenceMeasurements to write
   * @throws IOException if an error occurs while writing data
   */
  public void writeSelectedSequenceMeasurements(final SequenceMeasurements sm)
      throws IOException {

    this.measurementWriter.writeSequenceMesurement(sm);
  }

  /**
   * Close the the measurements reader and writer.
   * @throws IOException if an error occurs while closing the files.
   */
  public void close() throws IOException {

    this.measurementReader.close();
    this.measurementWriter.close();

  }

}
