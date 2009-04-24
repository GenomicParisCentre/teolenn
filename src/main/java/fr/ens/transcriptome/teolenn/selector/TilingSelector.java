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

package fr.ens.transcriptome.teolenn.selector;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import fr.ens.transcriptome.teolenn.Design;
import fr.ens.transcriptome.teolenn.Globals;
import fr.ens.transcriptome.teolenn.TeolennException;
import fr.ens.transcriptome.teolenn.WeightsSetter;
import fr.ens.transcriptome.teolenn.measurement.Measurement;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsIOFactory;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsReader;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsWriter;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurementsStatReader;

/**
 * This class implements the sequence selector for tiling design.
 * @author Laurent Jourdren
 */
public class TilingSelector implements SequenceSelector {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);
  private static final float MIN_SCORE = -1 * Float.MAX_VALUE;

  private File oriFile;
  private File filteredFile;
  private File statsFile;
  private File outputFile;
  private int windowLength;
  private int windowStep;
  private boolean start1;

  /**
   * Set a parameter for the filter.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  public void setInitParameter(String key, String value) {

    if (Design.START_1_PARAMETER_NAME.equals(key))
      this.start1 = Boolean.parseBoolean(value);

    if ("_filteredMesFile".equals(key))
      this.filteredFile = new File(value);
    if ("_oriMesFile".equals(key))
      this.oriFile = new File(value);

    if ("_statsFile".equals(key))
      this.statsFile = new File(value);

    if ("_selectedFile".equals(key))
      this.outputFile = new File(value);

    if ("_windowLength".equals(key))
      this.windowLength = Integer.parseInt(value.trim());

    if ("_windowStep".equals(key))
      this.windowStep = Integer.parseInt(value.trim());

  }

  /**
   * Run the initialization phase of the parameter.
   * @throws IOException if an error occurs while the initialization phase
   */
  public void init() throws IOException {

  }

  /**
   * Proceed to the oligonucleotide selection.
   * @param weightsSetters Weight to apply on measurements
   * @throws TeolennException if an error occurs while selecting sequences
   */
  public void select(final WeightsSetter weightsSetters)
      throws TeolennException {

    try {
      select2(weightsSetters);
    } catch (IOException e) {

      throw new TeolennException("Error while selecting: " + e.getMessage());
    }

  }

  /**
   * Proceed to the oligonucleotide selection.
   * @param weightsSetters Weight to apply on measurements
   * @throws TeolennException if an error occurs while selecting sequences
   */
  private void select2(final WeightsSetter weightsSetters) throws IOException {

    final int windowLength = this.windowLength;
    final int windowStep = this.windowStep;
    final boolean start1 = this.start1;

    // Open measurement file
    final SequenceMeasurementsReader smr =
        SequenceMeasurementsIOFactory.createSequenceMeasurementsFilteredReader(
            filteredFile, this.oriFile);

    // Object used to read oligo measurement
    SequenceMeasurements sm = null;

    boolean first = true;
    int indexScaffold = -1;
    int indexStartPosition = -1;
    String currentScafold = null;
    Object[] values = null;
    int valuesLength = 0;

    int infoLastIndexStartPosition = -1;
    int infoCountWindows = 1;
    int infoCountSelectedOligos = 0;

    int endWindow = windowLength + (start1 ? 1 : 0);
    int startWindow = start1 ? 1 : 0;

    float bestScore = MIN_SCORE;
    float nextBestScore = MIN_SCORE;
    int posNextBestScore = -1;
    int lastSelected = -1;

    // Create Object used to write selected oligo
    final SequenceMeasurements smToWrite = new SequenceMeasurements();

    final SequenceMeasurements nextSmToWrite = new SequenceMeasurements();

    // Open output file
    final SequenceMeasurementsWriter smw =
        SequenceMeasurementsIOFactory
            .createSequenceMeasurementsSelectWriter(this.outputFile);

    while ((sm = smr.next(sm)) != null) {

      if (first) {

        indexScaffold = sm.getIndexMeasurment("chromosome");
        indexStartPosition = sm.getIndexMeasurment("oligostart");
        values = sm.getArrayMeasurementValues();
        valuesLength = values.length;

        // Add measurement field in output file
        for (Measurement m : sm.getMeasurements()) {
          smToWrite.addMesurement(m);
          nextSmToWrite.addMesurement(m);
        }

        // Add Global score to the output file
        smToWrite.addMesurement(new GlobalScoreMeasurement());
        nextSmToWrite.addMesurement(new GlobalScoreMeasurement());

        // Read stats
        SequenceMeasurementsStatReader smsr =
            new SequenceMeasurementsStatReader(statsFile, sm);
        smsr.read();

        // Set the weights
        weightsSetters.setWeights(sm);

        if (sm.isSumOfWeightEquals1())
          logger.warning("Sum of weights is not equals to 1.");

        first = false;
      }

      if (indexScaffold < 0)
        throw new RuntimeException("No Scaffold field");
      if (indexStartPosition < 0)
        throw new RuntimeException("No Start field");

      final String chromosome = (String) values[indexScaffold];
      final int pos = (Integer) values[indexStartPosition];

      final int id = sm.getId();

      // final boolean debug = "86".equals(scaffold);

      if (currentScafold == null)
        currentScafold = chromosome;

      if (!currentScafold.equals(chromosome)) {

        // Write best
        if (bestScore > MIN_SCORE) {

          // Prevent writing the same oligo more than one time
          if (lastSelected != smToWrite.getId()) {

            smw.writeSequenceMesurement(smToWrite);
            lastSelected = smToWrite.getId();
            infoCountSelectedOligos++;
          }
        } else
          logger.severe("Bad case while selecting (1): " + bestScore);

        logger
            .fine(String
                .format(
                    "chromosome: %s\t%d windows (%.2f theoric), "
                        + "%d oligos selected, %d pb in chromosome, %d pb windows, %d pb step.",
                    currentScafold,
                    infoCountWindows,
                    ((infoLastIndexStartPosition + 1.0f - windowLength) / windowStep) + 1.0f,
                    infoCountSelectedOligos, infoLastIndexStartPosition,
                    windowLength, windowStep));

        infoCountSelectedOligos = 0;

        currentScafold = chromosome;
        endWindow = windowLength + (start1 ? 1 : 0);
        startWindow = start1 ? 1 : 0;
        infoCountWindows = 1;

        while (pos >= endWindow) {
          startWindow = endWindow + 1;
          endWindow += windowStep;
          infoCountWindows++;
        }

        bestScore = MIN_SCORE;
        nextBestScore = MIN_SCORE;
        posNextBestScore = -1;
      }

      if (pos >= endWindow) {
        // Write best
        if (bestScore > MIN_SCORE) {

          // Prevent writing the same oligo more than one time
          if (lastSelected != smToWrite.getId()) {
            smw.writeSequenceMesurement(smToWrite);
            lastSelected = smToWrite.getId();
            infoCountSelectedOligos++;
          }
        } else
          logger.severe("Bad case while selecting (2): " + bestScore);

        bestScore = MIN_SCORE;

        final int previousEndWindow = endWindow;

        while (pos >= endWindow) {
          startWindow = endWindow + 1;
          endWindow += windowStep;
          infoCountWindows++;
        }

        // Test if the best score is also the best score for next window
        if (endWindow - windowStep == previousEndWindow) {

          bestScore = nextBestScore;

          // Test if the best score is also the best score for next next
          // window
          if (posNextBestScore < startWindow + windowStep)
            nextBestScore = MIN_SCORE;
        }

      }

      final float score = sm.getScore();
      boolean bestScoreChanged = false;

      if (score > bestScore) {

        bestScore = score;
        smToWrite.setId(id);

        // Add the global score
        final Object[] valuesToWrite = new Object[valuesLength + 1];
        System.arraycopy(values, 0, valuesToWrite, 0, valuesLength);
        valuesToWrite[valuesLength] = score;

        smToWrite.setArrayMeasurementValues(valuesToWrite);
        bestScoreChanged = true;
      }

      if ((pos >= startWindow + windowStep) && score > nextBestScore) {

        nextBestScore = score;
        posNextBestScore = pos;
        nextSmToWrite.setId(id);

        if (bestScoreChanged) {

          nextSmToWrite.setArrayMeasurementValues(smToWrite
              .getArrayMeasurementValues());
        } else {

          // Add the global score
          final Object[] valuesToWrite = new Object[valuesLength + 1];
          System.arraycopy(values, 0, valuesToWrite, 0, valuesLength);
          valuesToWrite[valuesLength] = score;

          nextSmToWrite.setArrayMeasurementValues(valuesToWrite);
        }
      }

      infoLastIndexStartPosition = pos;
    }

    // Write best
    if (bestScore > MIN_SCORE) {

      // logger.fine("*** select "
      // + smToWrite.getId() + " for window " + infoCountWindows
      // + "\tbestScore=" + bestScore + " ***");

      // Prevent writing the same oligo more than one time
      if (lastSelected != smToWrite.getId())
        smw.writeSequenceMesurement(smToWrite);
    }

    logger
        .fine(String
            .format(
                "chromosome: %s\t%d windows (%.2f theoric), "
                    + "%d oligos selected, %d pb in chromosome, %d pb windows, %d pb step.",
                currentScafold, infoCountWindows,
                (float) infoLastIndexStartPosition / (float) windowLength,
                infoCountSelectedOligos, infoLastIndexStartPosition,
                windowLength, windowStep));

    smr.close();
    smw.close();
  }
}
