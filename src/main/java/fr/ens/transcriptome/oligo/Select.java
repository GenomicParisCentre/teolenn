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

import fr.ens.transcriptome.oligo.measurement.Measurement;
import fr.ens.transcriptome.oligo.measurement.ScaffoldMeasurement;

public class Select {

  private static final float MIN_SCORE = -1 * Float.MAX_VALUE;

  private int windowSize = 141;
  private int oligoSize = 60;
  private int windowBestPosition;

  private float positionScore(final int start) {

    final int windowStart =
        (int) (Math.floor((float) start / (float) this.windowSize)
            * this.windowSize - 1);
    final int oligo2Window = start - windowStart;

    final float result =
        1 - (Math.abs(this.windowBestPosition - oligo2Window) / ((float) this.windowSize - (float) this.windowBestPosition));

    return result;
  }

  public void select(final SequenceMeasurementReader smr) throws IOException {

    this.windowBestPosition = (int) Math.ceil((windowSize - oligoSize) / 2.0);

    SequenceMeasurements sm = null;

    boolean first = true;
    int indexStart = -1;

    while ((sm = smr.next(sm)) != null) {

      if (first) {

        indexStart = sm.getIndexMeasurment("Start");
        first = false;
      }

      if (indexStart == -1)
        throw new RuntimeException("Can't find oligoStart field");

      final Object[] values = sm.getArrayMeasurementValues();

      final int oligoStart = (Integer) values[indexStart];

      final float currentPositionScore = positionScore(oligoStart);
      final float currentOligoScore = sm.getScore();

    }
  }

  public static abstract class WeightsSetter {

    public abstract void setWeights(SequenceMeasurements sm);
  };

  public static void select2(final File inputFile, final File statsFile,
      File outputFile, final WeightsSetter weightsSetters, final int windowSize)
      throws IOException {

    final SequenceMeasurementReader smr =
        new SequenceMeasurementReader(inputFile);

    SequenceMeasurements sm = null;

    boolean first = true;
    int indexScaffold = -1;
    int indexStartPosition = -1;
    String currentScafold = null;
    Object[] values = null;

    int max = windowSize;

    float bestScore = MIN_SCORE;
    SequenceMeasurements smToWrite = new SequenceMeasurements();

    SequenceMeasurementsWriter smw = new SequenceMeasurementsWriter(outputFile);

    while ((sm = smr.next(sm)) != null) {

      if (first) {

        indexScaffold = sm.getIndexMeasurment("Scaffold");
        indexStartPosition = sm.getIndexMeasurment("Start");
        values = sm.getArrayMeasurementValues();

        for (Measurement m : sm.getMeasurements())
          smToWrite.addMesurement(m);

        SequenceMesurementsStatReader smsr =
            new SequenceMesurementsStatReader(statsFile, sm);
        smsr.read();

        weightsSetters.setWeights(sm);

        first = false;
      }

      if (indexScaffold < 0)
        throw new RuntimeException("No Scaffold field");
      if (indexStartPosition < 0)
        throw new RuntimeException("No Start field");

      final String scafold = (String) values[indexScaffold];
      final int pos = Integer.parseInt((String) values[indexStartPosition]);
      final int id = sm.getId();

      if (currentScafold == null)
        currentScafold = scafold;

      if (!currentScafold.equals(scafold)) {

        // Write best
        if (bestScore > MIN_SCORE)
          smw.writeSequenceMesurement(smToWrite);

        currentScafold = scafold;
        max = windowSize;
        while (pos > max)
          max += windowSize;

        bestScore = MIN_SCORE;
      }

      if (pos > max) {
        // Write best
        if (bestScore > MIN_SCORE)
          smw.writeSequenceMesurement(smToWrite);

        bestScore = MIN_SCORE;
        while (pos > max)
          max += windowSize;
      }

      final float score = sm.getScore();

      if (score > bestScore) {

        bestScore = score;
        smToWrite.setId(id);
        smToWrite.setArrayMeasurementValues(values.clone());
      }

    }

    // Write best
    if (bestScore > MIN_SCORE)
      smw.writeSequenceMesurement(smToWrite);

    smw.close();
  }

}
