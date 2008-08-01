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
import java.io.Writer;

import fr.ens.transcriptome.oligo.measurement.Measurement;

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

  public static abstract class WeightsSetter {

    public abstract void setWeights(SequenceMeasurements sm);
  };

  public static void select(final File inputFile, final File statsFile,
      File outputFile, final WeightsSetter weightsSetters, final int windowSize)
      throws IOException {

    // Open measurement file
    final SequenceMeasurementReader smr =
        new SequenceMeasurementReader(inputFile);

    // Object used to read oligo measurement
    SequenceMeasurements sm = null;

    boolean first = true;
    int indexScaffold = -1;
    int indexStartPosition = -1;
    String currentScafold = null;
    Object[] values = null;

    int infoLastIndexStartPosition = -1;
    int infoCountWindows = 1;
    int infoCountSelectedOligos = 0;

    int max = windowSize;

    float bestScore = MIN_SCORE;

    // Create Object used to write selected oligo
    final SequenceMeasurements smToWrite = new SequenceMeasurements();

    // Open output file
    final SequenceMeasurementsWriter smw =
        new SequenceMeasurementsWriter(outputFile);

    new ScoresWriter(new File(outputFile.getParentFile(), "scores.txt"));
    Writer writer = ScoresWriter.getSingleton().getWriter();

    while ((sm = smr.next(sm)) != null) {

      if (first) {

        indexScaffold = sm.getIndexMeasurment("Scaffold");
        indexStartPosition = sm.getIndexMeasurment("Start");
        values = sm.getArrayMeasurementValues();

        writer.write("id\t");

        // Add measurement field in output file
        for (Measurement m : sm.getMeasurements()) {
          smToWrite.addMesurement(m);

          writer.write(m.getName() + " value");
          writer.write("\t");
          writer.write(m.getName() + " score");
          writer.write("\t");
          writer.write(m.getName() + " score*w");
          writer.write("\t");
        }
        writer.write("final score\n");

        // Read stats
        SequenceMesurementsStatReader smsr =
            new SequenceMesurementsStatReader(statsFile, sm);
        smsr.read();

        // Set the weights
        weightsSetters.setWeights(sm);

        if (sm.isSumOfWeightEquals1())
          System.err.println("WARNING: Sum of weights is not equals to 1.");

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
        if (bestScore > MIN_SCORE) {
          smw.writeSequenceMesurement(smToWrite);

          infoCountSelectedOligos++;
        } else
          System.err.println("Erreur1: " + bestScore);

        // System.out.println("scaffold: "
        // + currentScafold + "\t" + infoCountWindows + " windows, "
        // + infoLastIndexStartPosition + " pb.\t("
        // + (infoLastIndexStartPosition / windowSize) + " theoric windows)");

        System.out.printf("scaffold: %s\t%d windows (%.2f theoric), "
            + "%d oligos selected, %d pb in scaffold, %d pb windows.\n",
            currentScafold, infoCountWindows,
            (float) infoLastIndexStartPosition / (float) windowSize,
            infoCountSelectedOligos, infoLastIndexStartPosition, windowSize);

        infoCountSelectedOligos = 0;

        currentScafold = scafold;
        max = windowSize;
        infoCountWindows = 1;

        while (pos > max) {
          max += windowSize;
          infoCountWindows++;
        }

        bestScore = MIN_SCORE;
      }

      if (pos > max) {
        // Write best
        if (bestScore > MIN_SCORE) {
          smw.writeSequenceMesurement(smToWrite);

          infoCountSelectedOligos++;
        } else
          System.err.println("Erreur2: " + bestScore);

        bestScore = MIN_SCORE;
        while (pos > max) {
          max += windowSize;
          infoCountWindows++;
        }
      }

      final float score = sm.getScore();

      if (score > bestScore) {

        bestScore = score;
        smToWrite.setId(id);
        smToWrite.setArrayMeasurementValues(values.clone());
      }

      // if ((pos - infoLastIndexStartPosition) > 1)
      // System.out.println("last: "
      // + infoLastIndexStartPosition + "\tpos=" + pos);

      infoLastIndexStartPosition = pos;
    }

    // Write best
    if (bestScore > MIN_SCORE)
      smw.writeSequenceMesurement(smToWrite);

    System.out.printf("scaffold: %s\t%d windows (%.2f theoric), "
        + "%d oligos selected, %d pb in scaffold, %d pb windows.\n",
        currentScafold, infoCountWindows, (float) infoLastIndexStartPosition
            / (float) windowSize, infoCountSelectedOligos,
        infoLastIndexStartPosition, windowSize);

    smw.close();
    writer.close();
  }

}
