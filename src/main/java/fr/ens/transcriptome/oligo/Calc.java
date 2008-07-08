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
import java.io.FilenameFilter;
import java.io.IOException;

import fr.ens.transcriptome.oligo.filter.FloatRangeFilter;
import fr.ens.transcriptome.oligo.filter.SequenceMeasurementFilter;
import fr.ens.transcriptome.oligo.measurement.GCPencentMeasurement;
import fr.ens.transcriptome.oligo.measurement.OligoStartMeasurement;
import fr.ens.transcriptome.oligo.measurement.PositionMeasurement;
import fr.ens.transcriptome.oligo.measurement.ScaffoldMeasurement;
import fr.ens.transcriptome.oligo.measurement.TmMeasurement;

public class Calc {

  public static final void createTmFile(final File inputFile,
      final File outputDir) throws IOException {

    String scafold = inputFile.getName().split("_")[1];

    File f = new File(outputDir, "scaffold." + scafold + ".tm2");

    final SequenceIterator si = new SequenceIterator(inputFile);
    SequenceMeasurementsWriter smw = new SequenceMeasurementsWriter(f);

    SequenceMeasurements sm = new SequenceMeasurements();
    sm.addMesurement(new ScaffoldMeasurement());
    sm.addMesurement(new OligoStartMeasurement());
    sm.addMesurement(new PositionMeasurement(141));
    sm.addMesurement(new TmMeasurement());
    sm.addMesurement(new GCPencentMeasurement());

    // smw.writeHeader(sm);

    int count = 0;

    while (si.hasNext()) {

      si.next();
      sm.setId(++count);
      sm.setSequence(si);
      sm.calcMesurements();
      sm.addMesurementsToStats();
      smw.writeSequenceMesurement(sm);

    }

    smw.close();

    SequenceMesurementsStatWriter smsw =
        new SequenceMesurementsStatWriter(new File(outputDir, "scaffold."
            + scafold + ".stats"));

    smsw.write(sm);

  }

  public static final void filterTmFile(final File inputFile,
      final File outputFile) throws IOException {

    SequenceMeasurementReader smr = new SequenceMeasurementReader(inputFile);

    SequenceMeasurementsWriter smw = new SequenceMeasurementsWriter(outputFile);

    SequenceMeasurements sm = null;
    SequenceMeasurements last = null;

    SequenceMeasurementFilter filter = new FloatRangeFilter("Tm", 70, 80);

    while ((sm = smr.next(sm)) != null) {

      if (filter.accept(sm)) {
        sm.addMesurementsToStats();
        smw.writeSequenceMesurement(sm);
      }

      last = sm;
    }

    smw.close();

    SequenceMesurementsStatWriter smsw =
        new SequenceMesurementsStatWriter(new File(outputFile.getParentFile(),
            outputFile.getName() + ".stats"));

    smsw.write(last);

  }

  public static void calcScore(final File inputFile, final File statsFile,
      final File outputFile) throws IOException {

    Select.WeightsSetter wSetter = new Select.WeightsSetter() {

      @Override
      public void setWeights(SequenceMeasurements sm) {

        sm.setWeight(sm.getMeasurement("Tm"), 0.4f);
        sm.setWeight(sm.getMeasurement("%GC"), 0.2f);
        sm.setWeight(sm.getMeasurement("Position"), 0.25f);

      }

    };

    Select.select2(inputFile, statsFile, outputFile, wSetter, 141);

  }

  public static void main(String[] args) throws IOException,
      InterruptedException {

    if (true) {

      File inputDir = new File("/home/jourdren/tmp/testseq/t2");
      File inputFile = new File(inputDir, "scaffold_87_1.fa2");
      File outputDir = new File("/home/jourdren/tmp/testseq/tm");

      createTmFile(inputFile, outputDir);
      filterTmFile(new File(outputDir, "scaffold.87.tm2"), new File(outputDir,
          "scaffold.87.tm3"));

      calcScore(new File(outputDir, "scaffold.87.tm2"), new File(outputDir,
          "scaffold.87.stats"), new File(outputDir, "scaffold.87.results"));

      System.exit(0);
    }

    // File inputDir = new File("/home/jourdren/tmp/testseq/t2");
    // File outputDir = new File("/home/jourdren/tmp/testseq/tm");

    // Thread.sleep(20000);

    File inputDir = new File(args[0]);
    File outputDir = new File(args[1]);

    File[] files = inputDir.listFiles(new FilenameFilter() {

      public boolean accept(File dir, String name) {

        return name.endsWith(".fa2");
      }
    });

    for (int i = 0; i < files.length; i++) {

      createTmFile(files[i], outputDir);
    }

  }
}
