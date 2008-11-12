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
import java.io.IOException;
import java.util.logging.Logger;

import fr.ens.transcriptome.oligo.measurement.Measurement;
import fr.ens.transcriptome.oligo.measurement.SimpleMeasurement;

/**
 * This class implements the algorithm used for oligo selection
 * @author Laurent Jourdren
 */
public class Select {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  private static final float MIN_SCORE = -1 * Float.MAX_VALUE;

  /**
   * This abstract class allow the user to set weights and other parameters
   * before selection.
   * @author Laurent Jourdren
   */
  public static abstract class WeightsSetter {

    /**
     * Define the weigths to use for selection
     * @param sm SequenceMeasurement that contains all the measurements to use
     *          by the selection algorithm.
     */
    public abstract void setWeights(final SequenceMeasurements sm);
  };

  private static class GlobalScoreMeasurement extends SimpleMeasurement {

    /**
     * Calc the measurement of a sequence.
     * @param sequence the sequence to use for the measurement
     * @return an object as result
     */
    public Object calcMesurement(final Sequence sequence) {
      return null;
    }

    /**
     * Get the description of the measurement.
     * @return the description of the measurement
     */
    public String getDescription() {
      return "Global Score";
    }

    /**
     * Get the name of the measurement.
     * @return the name of the measurement
     */
    public String getName() {
      return "GlobalScore";
    }

    /**
     * Get the type of the result of calcMeasurement.
     * @return the type of the measurement
     */
    public Object getType() {
      return Float.class;
    }

    /**
     * Run the initialization phase of the parameter.
     * @throws IOException if an error occurs while the initialization phase
     */
    public void init() throws IOException {
    }

    /**
     * Parse a string to an object return as calcMeasurement.
     * @param s String to parse
     * @return an object
     */
    public Object parse(String s) {
      return null;
    }

    /**
     * Set a parameter for the filter.
     * @param key key for the parameter
     * @param value value of the parameter
     */
    public void setInitParameter(String key, String value) {
    }

  }

  /**
   * Proceed to the oligo selection.
   * @param inputFile measurements input file
   * @param statsFile stats file for the measurements input file
   * @param outputFile output file to generate with the selected oligos
   * @param weightsSetters Weight to apply on measurements
   * @param windowLength size of the window
   * @param windowStep step for the windows
   * @throws IOException if an error occurs while reading/writing input/output
   *           files
   */
  public static void select(final File inputFile, final File statsFile,
      File outputFile, final WeightsSetter weightsSetters,
      final int windowLength, final int windowStep) throws IOException {

    // Open measurement file
    final SequenceMeasurementsReader smr =
        new SequenceMeasurementsReader(inputFile);

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

    int endWindow = windowLength;
    int startWindow = 0;

    float bestScore = MIN_SCORE;
    float nextBestScore = MIN_SCORE;
    int posNextBestScore = -1;
    int lastSelected = -1;

    // Create Object used to write selected oligo
    final SequenceMeasurements smToWrite = new SequenceMeasurements();

    final SequenceMeasurements nextSmToWrite = new SequenceMeasurements();

    // Open output file
    final SequenceMeasurementsWriter smw =
        new SequenceMeasurementsWriter(outputFile);

    while ((sm = smr.next(sm)) != null) {

      if (first) {

        indexScaffold = sm.getIndexMeasurment("scaffold");
        indexStartPosition = sm.getIndexMeasurment("start");
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

      final String scaffold = (String) values[indexScaffold];
      final int pos = (Integer) values[indexStartPosition];

      final int id = sm.getId();

      // final boolean debug = "86".equals(scaffold);

      if (currentScafold == null)
        currentScafold = scaffold;

      if (!currentScafold.equals(scaffold)) {

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
                    "scaffold: %s\t%d windows (%.2f theoric), "
                        + "%d oligos selected, %d pb in scaffold, %d pb windows, %d pb step.",
                    currentScafold,
                    infoCountWindows,
                    ((infoLastIndexStartPosition + 1.0f - windowLength) / windowStep) + 1.0f,
                    infoCountSelectedOligos, infoLastIndexStartPosition,
                    windowLength, windowStep));

        infoCountSelectedOligos = 0;

        currentScafold = scaffold;
        endWindow = windowLength;
        startWindow = 0;
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

          // Test if the best score is also the best score for next next window
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

    logger.fine(String.format("scaffold: %s\t%d windows (%.2f theoric), "
        + "%d oligos selected, %d pb in scaffold, %d pb windows, %d pb step.",
        currentScafold, infoCountWindows, (float) infoLastIndexStartPosition
            / (float) windowLength, infoCountSelectedOligos,
        infoLastIndexStartPosition, windowLength, windowStep));

    smw.close();

  }

}
