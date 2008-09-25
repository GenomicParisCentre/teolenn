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
import java.util.List;

import fr.ens.transcriptome.oligo.filter.RedundancyFilter;
import fr.ens.transcriptome.oligo.filter.SequenceFilter;
import fr.ens.transcriptome.oligo.filter.SequenceXNFilter;
import fr.ens.transcriptome.oligo.measurement.ComplexityMeasurement;
import fr.ens.transcriptome.oligo.measurement.GCPencentMeasurement;
import fr.ens.transcriptome.oligo.measurement.Measurement;
import fr.ens.transcriptome.oligo.measurement.OligoSequenceMeasurement;
import fr.ens.transcriptome.oligo.measurement.OligoStartMeasurement;
import fr.ens.transcriptome.oligo.measurement.PositionMeasurement;
import fr.ens.transcriptome.oligo.measurement.ScaffoldMeasurement;
import fr.ens.transcriptome.oligo.measurement.TmMeasurement;
import fr.ens.transcriptome.oligo.measurement.UnicityMeasurement;
import fr.ens.transcriptome.oligo.measurement.filter.FloatRangeFilter;
import fr.ens.transcriptome.oligo.measurement.filter.SequenceMeasurementFilter;
import fr.ens.transcriptome.oligo.util.StringUtils;

public class Design {

  private static int WINDOW_SIZE = 140; // 141;
  private static int OLIGO_SIZE = 60;
  private static int UNICITY_MAX_PREFIX_LEN = 30;

  private static String OLIGO_SUFFIX = ".oligo";
  private static String OLIGO_MASKED_SUFFIX = ".masked";
  private static String OLIGO_FILTERED_SUFFIX = ".filtered.oligo";
  private static String OLIGO_MASKED_FILTERED_SUFFIX = ".filtered.masked";

  /**
   * Create a measurement file.
   * @param inputFiles oligo input fasta files
   * @param measurementsFile output file
   * @param statsFile statFile to create (optional)
   * @throws IOException if an error occurs while creating the measurement
   */
  public static final void createMeasurementsFile(final File[] inputFiles,
      final File measurementsFile, final List<Measurement> measurements,
      final File statsFile) throws IOException {

    if (inputFiles == null || inputFiles.length == 0)
      return;

    final SequenceMeasurementsWriter smw =
        new SequenceMeasurementsWriter(measurementsFile);

    final SequenceMeasurements sm = new SequenceMeasurements();
    if (measurements != null)
      for (Measurement m : measurements)
        sm.addMesurement(m);

    int id = 0;

    for (int i = 0; i < inputFiles.length; i++)
      id = createMeasurementsFile(inputFiles[i], smw, sm, id);

    smw.close();

    // Create a stat file if needed
    if (statsFile != null) {
      SequenceMesurementsStatWriter smsw =
          new SequenceMesurementsStatWriter(statsFile);

      smsw.write(sm);
    }

  }

  private static final int createMeasurementsFile(final File inputFile,
      final SequenceMeasurementsWriter smw, final SequenceMeasurements sm,
      final int idStart) throws IOException {

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

  /**
   * Filter oligos fasta files
   * @param oligoFiles input file
   * @param sequenceFilters filters to apply
   * @throws IOException if an error occurs while filtering
   */
  public static final void filterSequencesFiles(final File[] oligoFiles,
      final List<SequenceFilter> sequenceFilters) throws IOException {

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

  /**
   * Filter a measurement file.
   * @param measurementsFile input file
   * @param filteredMeasurementsFile output file
   * @param statsFile statFile to create (optional)
   * @param filters Filters to applys
   * @throws IOException if an error occurs while filtering
   */
  public static final void filterMeasurementsFile(final File measurementsFile,
      final File filteredMeasurementsFile, final File statsFile,
      final List<SequenceMeasurementFilter> filters) throws IOException {

    final SequenceMeasurementReader smr =
        new SequenceMeasurementReader(measurementsFile);

    final SequenceMeasurementsWriter smw =
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
      } else
        System.out.println(sm.getId()
            + "\t" + Arrays.toString(sm.getArrayMeasurementValues()));

      last = sm;
    }

    smw.close();

    // Create a stat file if needed
    if (statsFile != null) {
      SequenceMesurementsStatWriter smsw =
          new SequenceMesurementsStatWriter(statsFile);

      smsw.write(last);
    }

  }

  public static void main(String[] args) throws IOException {

    System.out.println(Globals.APP_NAME + " " + Globals.APP_VERSION + "\n");

    try {
      Thread.sleep(0);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    if (args == null || args.length == 0) {
      System.err.println("No genome file.");
      System.exit(1);
    }

    if (args.length > 1) {
      System.err.println("Too many genomes files.");
      System.exit(1);
    }

    // Create input file objects

    File genomeFile = new File(args[0]);

    File genomeMaskedFile =
        new File(StringUtils.basename(genomeFile.getAbsolutePath())
            + ".allmasked");

    System.out.println("Genome file: " + genomeFile);

    File outputDir = genomeFile.getAbsoluteFile().getParentFile();

    //
    // Test params
    // 

    boolean filterOnly = true;
    boolean seqFilter = true;
    boolean overvriteStatFile = true;

    if (!filterOnly) {

      // Create oligo sequences
      FastaOverlap2.fastaOverlap(genomeFile, outputDir, OLIGO_SUFFIX,
          OLIGO_SIZE);

      FastaOverlap2.fastaOverlap(genomeMaskedFile, outputDir,
          OLIGO_MASKED_SUFFIX, OLIGO_SIZE);

      // Filter sequences
      File[] oligoFiles = outputDir.listFiles(new FilenameFilter() {

        public boolean accept(File dir, String name) {

          return name.endsWith(OLIGO_SUFFIX);
        }
      });

      final SequenceFilter uf =
          seqFilter ? new RedundancyFilter(genomeFile, oligoFiles) : null;

      filterSequencesFiles(oligoFiles, seqFilter ? Arrays.asList(uf,
          new SequenceXNFilter()) : Arrays.asList(new SequenceFilter[] {}));

      File[] oligoFilteredFiles = outputDir.listFiles(new FilenameFilter() {

        public boolean accept(File dir, String name) {

          return name.endsWith(OLIGO_FILTERED_SUFFIX);
        }
      });

      Arrays.sort(oligoFilteredFiles);

      // Create the list of measurement to compute
      final List<Measurement> measurements = new ArrayList<Measurement>();
      measurements.add(new ScaffoldMeasurement());
      measurements.add(new OligoStartMeasurement());
      measurements.add(new OligoSequenceMeasurement());
      measurements.add(new PositionMeasurement(WINDOW_SIZE));
      measurements.add(new TmMeasurement());
      measurements.add(new GCPencentMeasurement());
      measurements.add(new ComplexityMeasurement());
      measurements.add(new UnicityMeasurement(genomeFile, OLIGO_SIZE,
          UNICITY_MAX_PREFIX_LEN));

      // Calc oligos measurements
      File oligoMeasurementsFile = new File(outputDir, "oligo.mes");
      createMeasurementsFile(oligoFilteredFiles, oligoMeasurementsFile,
          measurements, null);

    }

    // Duplicate because filterOnly
    File oligoMeasurementsFile = new File(outputDir, "oligo.mes");

    // Filter oligos measurements

    // Define a list of filters
    List<SequenceMeasurementFilter> filters =
        new ArrayList<SequenceMeasurementFilter>();
    filters.add(new FloatRangeFilter("%GC", 0, 1));
    filters.add(new FloatRangeFilter("Tm", 0, 100));
    filters.add(new FloatRangeFilter("Complexity", 0, 1));
    filters.add(new FloatRangeFilter("Unicity", 0, OLIGO_SIZE));

    File filteredOligoMeasurementsFile = new File(outputDir, "filtered.mes");
    File statsFile = new File(outputDir, "filtered.stats");

    filterMeasurementsFile(oligoMeasurementsFile,
        filteredOligoMeasurementsFile, overvriteStatFile ? statsFile : null,
        filters);

    // Select oligos

    File selectedOligos = new File(outputDir, "select.mes");

    // Define the weights of the measurements
    Select.WeightsSetter wSetter = new Select.WeightsSetter() {

      public void setWeights(SequenceMeasurements sm) {

        sm.setWeight(sm.getMeasurement("Tm"), 0.4f * 0.75f);
        sm.setWeight(sm.getMeasurement("%GC"), 0.2f * 0.75f);
        sm.setWeight(sm.getMeasurement("Complexity"), 0.1f * 0.75f);
        sm.setWeight(sm.getMeasurement("Unicity"), 0.3f * 0.75f);
        sm.setWeight(sm.getMeasurement("Position"), 0.25f);

        sm.getMeasurement("Tm").setProperty("reference", "" + 74.32);
        sm.getMeasurement("Tm").setProperty("deviation", "" + 24.4);
        sm.getMeasurement("%GC").setProperty("reference", "" + 0.5333);
        sm.getMeasurement("%GC").setProperty("deviation", "" + 0.3333);

        sm.getMeasurement("Unicity").setProperty("max", "" + 42);
      }

    };

    Select.select(filteredOligoMeasurementsFile, statsFile, selectedOligos,
        wSetter, WINDOW_SIZE);

  }
}
