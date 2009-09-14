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

import java.io.IOException;
import java.util.logging.Logger;

import fr.ens.transcriptome.teolenn.DesignConstants;
import fr.ens.transcriptome.teolenn.Globals;
import fr.ens.transcriptome.teolenn.TeolennException;
import fr.ens.transcriptome.teolenn.measurement.ChromosomeMeasurement;
import fr.ens.transcriptome.teolenn.measurement.Measurement;
import fr.ens.transcriptome.teolenn.measurement.OligoStartMeasurement;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;

/**
 * This class define the tiling selector.
 * @author Laurent Jourdren
 */
public class TilingSelector extends SimpleSelector {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  /** Selector name. */
  public static final String SELECTOR_NAME = "tiling";

  private static final float MIN_SCORE = -1 * Float.MAX_VALUE;

  private boolean start1;
  private int oligoLength;
  private int windowLength = -1;
  private int windowStep = -1;

  /**
   * Get the name of the selector.
   * @return the name of the selector
   */
  public String getName() {

    return SELECTOR_NAME;
  }

  /**
   * Get the description of the selector.
   * @return the description of the selector
   */
  public String getDescription() {

    return "A Selector for tiling design";
  }

  /**
   * Set a parameter for the selector.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  public void setInitParameter(final String key, final String value) {

    if (DesignConstants.START_1_PARAMETER_NAME.equals(key))
      this.start1 = Boolean.parseBoolean(value);

    else if (DesignConstants.OLIGO_LENGTH_PARAMETER_NAME.equals(key))
      this.oligoLength = Integer.parseInt(value.trim());

    else if ("windowLength".equals(key))
      this.windowLength = Integer.parseInt(value.trim());

    else if ("windowStep".equals(key))
      this.windowStep = Integer.parseInt(value.trim());

    super.setInitParameter(key, value);
  }

  public void init() throws TeolennException {

    if (this.windowLength < 1)
      throw new TeolennException("Invalid window length: " + this.windowLength);

    if (this.windowStep == -1)
      this.windowStep = this.windowLength;
    else if (this.windowStep < 1)
      throw new TeolennException("Invalid window step: " + this.windowStep);

    addMeasurement(new PositionMeasurement(this.start1, this.oligoLength,
        this.windowLength));
    addMeasurement(new TilingZoneMeasurement());
  }

  public void doSelection() throws IOException {

    logger.info("TilingSelector, windowLength=" + this.windowLength);
    logger.info("TilingSelector, windowStep=" + this.windowStep);
    logger.finest("TilingSelector, start1=" + this.start1);
    logger.finest("TilingSelector, oligoLength=" + this.oligoLength);

    boolean first = true;
    int indexScaffold = -1;
    int indexStartPosition = -1;
    int indexTilingZoneMeasurement = -1;

    String currentScafold = null;
    int infoLastIndexStartPosition = -1;
    int infoCountWindows = 1;
    int infoCountSelectedOligos = 0;

    final int windowLength = this.windowLength;
    int endWindow = windowLength - 1 + (start1 ? 1 : 0);
    int startWindow = start1 ? 1 : 0;
    String tilingZone = "[" + startWindow + "-" + endWindow + "]";

    float bestScore = MIN_SCORE;
    float nextBestScore = MIN_SCORE;
    int posNextBestScore = -1;
    int lastSelected = -1;
    int valuesLength = -1;

    // Object used to read oligo measurement
    SequenceMeasurements sm = null;
    Object[] values = null;

    final SequenceMeasurements smToWrite = new SequenceMeasurements();
    final SequenceMeasurements nextSmToWrite = new SequenceMeasurements();

    while ((sm = next()) != null) {

      if (first) {

        indexScaffold =
            sm.getIndexMeasurment(ChromosomeMeasurement.MEASUREMENT_NAME);
        indexStartPosition =
            sm.getIndexMeasurment(OligoStartMeasurement.MEASUREMENT_NAME);
        indexTilingZoneMeasurement =
            sm.getIndexMeasurment(TilingZoneMeasurement.MEASUREMENT_NAME);
        values = sm.getArrayMeasurementValues();

        for (Measurement m : sm.getMeasurements()) {
          smToWrite.addMesurement(m);
          nextSmToWrite.addMesurement(m);
        }

        valuesLength = values.length;
        first = false;
      }

      if (indexScaffold < 0)
        throw new RuntimeException("No Scaffold field");
      if (indexStartPosition < 0)
        throw new RuntimeException("No Start field");

      final String chromosome = (String) values[indexScaffold];
      final int pos = (Integer) values[indexStartPosition];

      final int id = sm.getId();

      if (currentScafold == null)
        currentScafold = chromosome;

      if (!currentScafold.equals(chromosome)) {

        // Write best
        if (bestScore > MIN_SCORE) {

          // Prevent writing the same oligo more than one time
          if (lastSelected != smToWrite.getId()) {

            writeSelectedSequenceMeasurements(smToWrite);
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
        endWindow = windowLength - 1 + (start1 ? 1 : 0);
        startWindow = start1 ? 1 : 0;
        infoCountWindows = 1;

        while (pos >= endWindow) {
          startWindow = endWindow + 1;
          endWindow += windowStep;
          infoCountWindows++;
        }
        tilingZone = "[" + startWindow + "-" + endWindow + "]";

        bestScore = MIN_SCORE;
        nextBestScore = MIN_SCORE;
        posNextBestScore = -1;
      }

      if (pos >= endWindow) {
        // Write best
        if (bestScore > MIN_SCORE) {

          // Prevent writing the same oligo more than one time
          if (lastSelected != smToWrite.getId()) {

            writeSelectedSequenceMeasurements(smToWrite);
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
        tilingZone = "[" + startWindow + "-" + endWindow + "]";

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
        values[indexTilingZoneMeasurement] = tilingZone;

        // Add the global score
        final Object[] valuesToWrite = new Object[valuesLength];
        System.arraycopy(values, 0, valuesToWrite, 0, valuesLength);
        // valuesToWrite[valuesLength] = score;

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

          values[indexTilingZoneMeasurement] = tilingZone;

          // Add the global score
          final Object[] valuesToWrite = new Object[valuesLength];
          System.arraycopy(values, 0, valuesToWrite, 0, valuesLength);
          // valuesToWrite[valuesLength] = score;

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
        writeSelectedSequenceMeasurements(smToWrite);
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

    close();

  }

}
