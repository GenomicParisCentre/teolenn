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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import fr.ens.transcriptome.oligo.filter.SequenceFilter;
import fr.ens.transcriptome.oligo.filter.SequenceMeasurementFilter;
import fr.ens.transcriptome.oligo.filter.UniquenessFilter;
import fr.ens.transcriptome.oligo.measurement.ComplexityMeasurement;
import fr.ens.transcriptome.oligo.measurement.GCPencentMeasurement;
import fr.ens.transcriptome.oligo.measurement.Measurement;
import fr.ens.transcriptome.oligo.measurement.OligoStartMeasurement;
import fr.ens.transcriptome.oligo.measurement.PositionMeasurement;
import fr.ens.transcriptome.oligo.measurement.ScaffoldMeasurement;
import fr.ens.transcriptome.oligo.measurement.TmMeasurement;
import fr.ens.transcriptome.oligo.util.StringUtils;

public class Design {

  private static int WINDOW_SIZE = 141;
  private static int OLIGO_SIZE = 60;
  private static String OLIGO_SUFFIX = ".oligo";
  private static String OLIGO_MASKED_SUFFIX = ".masked";
  private static String OLIGO_FILTERED_SUFFIX = ".filtered.oligo";
  private static String OLIGO_MASKED_FILTERED_SUFFIX = ".filtered.masked";

  public static final void createMeasurementsFile(final File[] inputFiles,
      final File measurementsFile) throws IOException {

    if (inputFiles == null || inputFiles.length == 0)
      return;

    SequenceMeasurementsWriter smw =
        new SequenceMeasurementsWriter(measurementsFile);

    SequenceMeasurements sm = new SequenceMeasurements();
    sm.addMesurement(new ScaffoldMeasurement());
    sm.addMesurement(new OligoStartMeasurement());
    sm.addMesurement(new PositionMeasurement(WINDOW_SIZE));
    sm.addMesurement(new TmMeasurement());
    sm.addMesurement(new GCPencentMeasurement());
    sm.addMesurement(new ComplexityMeasurement());

    int id = 0;

    for (int i = 0; i < inputFiles.length; i++)
      id = createMeasurementsFile(inputFiles[i], smw, sm, id);

    smw.close();

  }

  private static final int createMeasurementsFile(final File inputFile,
      SequenceMeasurementsWriter smw, SequenceMeasurements sm, final int idStart)
      throws IOException {

    final SequenceIterator si = new SequenceIterator(inputFile);

    // smw.writeHeader(sm);

    int count = idStart;

    for (Measurement m : sm.getMeasurements())
      m.setProperty("currentOligoFile", inputFile.getAbsolutePath());

    while (si.hasNext()) {

      si.next();
      sm.setId(++count);
      sm.setSequence(si);
      sm.calcMesurements();
      smw.writeSequenceMesurement(sm);

    }

    return count;
  }

  public static final void filterSequencesFiles(File[] oligoFiles,
      List<SequenceFilter> sequenceFilters) throws IOException {

    for (int i = 0; i < oligoFiles.length; i++) {

      final File oligoFile = oligoFiles[i];
      final String basename = StringUtils.basename(oligoFile.getAbsolutePath());

      final SequenceIterator si1 = new SequenceIterator(oligoFile);
      final SequenceIterator si2 =
          new SequenceIterator(new File(basename + OLIGO_MASKED_SUFFIX));
      final SequenceWriter sw1 =
          new SequenceWriter(new File(basename + OLIGO_FILTERED_SUFFIX));
      final SequenceWriter sw2 =
          new SequenceWriter(new File(basename + OLIGO_MASKED_FILTERED_SUFFIX));

      final SequenceFilter[] filters =
          sequenceFilters.toArray(new SequenceFilter[0]);
      while (si1.hasNext()) {

        si1.next();
        si2.next();

        boolean result = true;

        for (int j = 0; j < filters.length; j++)
          if (!filters[j].accept(si1)) {
            result = false;
            break;
          }

        if (result) {
          sw1.write(si1);
          sw2.write(si2);
        }

      }

      sw1.close();
      sw2.close();
    }

  }

  public static final void filterMeasurementsFile(final File measurementsFile,
      final File filteredMeasurementsFile, final File statsFile,
      final List<SequenceMeasurementFilter> filters) throws IOException {

    SequenceMeasurementReader smr =
        new SequenceMeasurementReader(measurementsFile);

    SequenceMeasurementsWriter smw =
        new SequenceMeasurementsWriter(filteredMeasurementsFile);

    SequenceMeasurements sm = null;
    SequenceMeasurements last = null;

    while ((sm = smr.next(sm)) != null) {

      boolean pass = true;

      for (SequenceMeasurementFilter filter : filters)
        if (!filter.accept(sm)) {
          pass = false;
          break;
        }

      if (pass) {
        sm.addMesurementsToStats();
        smw.writeSequenceMesurement(sm);
      }

      last = sm;
    }

    smw.close();

    SequenceMesurementsStatWriter smsw =
        new SequenceMesurementsStatWriter(statsFile);

    smsw.write(last);

  }

  public static void main(String[] args) throws IOException {

    try {
      Thread.sleep(0);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // args =
    // new String[] {"/home/jourdren/tmp/testseq/finaltest/scaffold_12.fasta",
    // "/home/jourdren/tmp/testseq/finaltest/scaffold_87.fasta",
    // "/home/jourdren/tmp/testseq/finaltest/scaffold_86.fasta"};

    if (args == null || args.length == 0) {
      System.err.println("No genome files.");
      System.exit(1);
    }

    // Create input file objects

    File[] genomeFiles = new File[args.length];

    for (int i = 0; i < args.length; i++)
      genomeFiles[i] = new File(args[i]);

    File[] genomeMaskedFiles = new File[args.length];

    for (int i = 0; i < args.length; i++)
      genomeMaskedFiles[i] =
          new File(StringUtils.basename(args[i]) + ".allmasked");

    System.out.println(Arrays.toString(genomeFiles));

    File outputDir = genomeFiles[0].getAbsoluteFile().getParentFile();

    // Create oligo sequences

    for (int i = 0; i < genomeFiles.length; i++)
      FastaOverlap2.fastaOverlap(genomeFiles[i], outputDir, OLIGO_SUFFIX,
          OLIGO_SIZE);

    for (int i = 0; i < genomeMaskedFiles.length; i++)
      FastaOverlap2.fastaOverlap(genomeMaskedFiles[i], outputDir,
          OLIGO_MASKED_SUFFIX, OLIGO_SIZE);

    // Filter sequences

    File[] oligoFiles = outputDir.listFiles(new FilenameFilter() {

      public boolean accept(File dir, String name) {

        return name.endsWith(OLIGO_SUFFIX);
      }
    });

    SequenceFilter uf = new UniquenessFilter(genomeFiles[0], oligoFiles);
    filterSequencesFiles(oligoFiles, Collections.singletonList(uf));

    File[] oligoFilteredFiles = outputDir.listFiles(new FilenameFilter() {

      public boolean accept(File dir, String name) {

        return name.endsWith(OLIGO_FILTERED_SUFFIX);
      }
    });

    Arrays.sort(oligoFilteredFiles);

    // Calc oligos measurements
    File oligoMeasurementsFile = new File(outputDir, "oligo.mes");
    createMeasurementsFile(oligoFilteredFiles, oligoMeasurementsFile);

    // Filter oligos measurements

    List<SequenceMeasurementFilter> filters =
        new ArrayList<SequenceMeasurementFilter>();
    // filters.add(new FloatRangeFilter("Tm", 50, 90));
    // filters.add(new FloatRangeFilter("%GC", .4f, .6f));

    File filteredOligoMeasurementsFile = new File(outputDir, "filtered.mes");
    File statsFile = new File(outputDir, "filtered.stats");

    filterMeasurementsFile(oligoMeasurementsFile,
        filteredOligoMeasurementsFile, statsFile, filters);

    // Select oligos

    File selectedOligos = new File(outputDir, "select.mes");
    Select.WeightsSetter wSetter = new Select.WeightsSetter() {

      public void setWeights(SequenceMeasurements sm) {

        sm.setWeight(sm.getMeasurement("Tm"), 0.66f * 0.75f);
        sm.setWeight(sm.getMeasurement("%GC"), 0.33f * 0.75f);
        sm.setWeight(sm.getMeasurement("Complexity"), 0.1f * 0.75f);
        sm.setWeight(sm.getMeasurement("Position"), 0.25f);

        // sm.getMeasurement("Tm").setProperty("sd", ""+24.4);
      }

    };

    Select.select2(filteredOligoMeasurementsFile, statsFile, selectedOligos,
        wSetter, WINDOW_SIZE);

  }

}
