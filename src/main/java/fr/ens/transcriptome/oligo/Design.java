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
import fr.ens.transcriptome.oligo.measurement.filter.MeasurementFilter;
import fr.ens.transcriptome.oligo.util.FileUtils;
import fr.ens.transcriptome.oligo.util.StringUtils;

public class Design {

  private static int WINDOW_SIZE_DEFAULT = 140; // 141;
  private static int OLIGO_SIZE_DEFAULT = 60;
  private static int UNICITY_MAX_PREFIX_LEN = 30;

  public static String OLIGO_SUFFIX = ".oligo";
  public static String OLIGO_MASKED_SUFFIX = ".masked";
  public static String OLIGO_FILTERED_SUFFIX = ".filtered.oligo";
  public static String OLIGO_MASKED_FILTERED_SUFFIX = ".filtered.masked";

  private static final String OLIGO_MEASUREMENTS_FILE = "oligo.mes";
  private static final String OLIGO_MEASUREMENTS_FILTERED_FILE = "filtered.mes";
  private static final String OLIGO_MEASUREMENTS_FILTERED_STATS_FILE =
      "filtered.stats";
  private static final String SELECTED_FILE = "select.mes";

  private int windowSize = WINDOW_SIZE_DEFAULT;
  private int oligoLength = OLIGO_SIZE_DEFAULT;

  private File genomeFile;
  private File genomeMaskedFile;
  private File outputDir;

  //
  // Getters
  //

  /**
   * Get the window size of the design.
   * @return the window size
   */
  public int getWindowSize() {
    return windowSize;
  }

  /**
   * Set the oligo length for the design
   * @return the oligo length
   */
  public int getOligoLength() {
    return oligoLength;
  }

  /**
   * Get the genome file.
   * @return the genome file
   */
  public File getGenomeFile() {
    return genomeFile;
  }

  /**
   * Get the genome masked file
   * @return the genome masked file
   */
  public File getGenomeMaskedFile() {
    return genomeMaskedFile;
  }

  /**
   * Set the output directory
   * @return the output directory
   */
  public File getOutputDir() {
    return outputDir;
  }

  //
  // Setters
  //

  /**
   * Set the window size.
   * @param windowSize the window size to set
   */
  public void setWindowSize(final int windowSize) {
    this.windowSize = windowSize;
  }

  /**
   * Set the olgo length
   * @param oligoLength The oligo length
   */
  public void setOligoLength(final int oligoLength) {
    this.oligoLength = oligoLength;
  }

  /**
   * Set the genome file
   * @param genomeFile Genome file to set
   */
  public void setGenomeFile(final File genomeFile) {
    this.genomeFile = genomeFile;
  }

  /**
   * Set the genome masked file
   * @param genomeMaskedFile the genome masked file
   */
  public void setGenomeMaskedFile(final File genomeMaskedFile) {
    this.genomeMaskedFile = genomeMaskedFile;
  }

  /**
   * Set the output directory
   * @param outputDir The output directory
   */
  public void setOutputDir(final File outputDir) {
    this.outputDir = outputDir;
  }

  //
  // Other methods
  //

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
      final List<MeasurementFilter> filters) throws IOException {

    final SequenceMeasurementReader smr =
        new SequenceMeasurementReader(measurementsFile);

    final SequenceMeasurementsWriter smw =
        new SequenceMeasurementsWriter(filteredMeasurementsFile);

    SequenceMeasurements sm = null;
    SequenceMeasurements last = null;

    while ((sm = smr.next(sm)) != null) {

      boolean pass = true;

      for (MeasurementFilter filter : filters)
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

  /**
   * In this phase, create all the oligos.
   * @throws IOException if an error occurs while creating all oligos
   */
  public void phase1CreateAllOligos() throws IOException {

    FastaOverlap.fastaOverlap(this.genomeFile, outputDir, Design.OLIGO_SUFFIX,
        this.oligoLength);

    FastaOverlap.fastaOverlap(this.genomeMaskedFile, outputDir,
        Design.OLIGO_MASKED_SUFFIX, this.oligoLength);
  }

  /**
   * In this phase, remove from the generated oligos all the invalid oligos.
   * @param listSequenceFilters list of sequence filters to apply
   * @throws IOException if an error occus while filtering
   */
  public void phase2FilterAllOligos(
      final List<SequenceFilter> listSequenceFilters) throws IOException {

    // Get the list of oligos files to process
    final File[] oligoFiles =
        FileUtils.listFilesByExtension(this.outputDir, Design.OLIGO_SUFFIX);
    Arrays.sort(oligoFiles);

    Design.filterSequencesFiles(oligoFiles, listSequenceFilters);
  }

  /**
   * In this phase, compute all the measurements of the oligos.
   * @param listMeasurements list of measurements to compute
   * @throws IOException if an error occus while filtering
   */
  public void phase3CalcMeasurements(final List<Measurement> listMeasurements)
      throws IOException {

    // Get the list of filtered oligos files to process
    final File[] oligoFilteredFiles =
        FileUtils.listFilesByExtension(this.outputDir,
            Design.OLIGO_FILTERED_SUFFIX);
    Arrays.sort(oligoFilteredFiles);

    // Calc oligos measurements
    File oligoMeasurementsFile = new File(outputDir, OLIGO_MEASUREMENTS_FILE);
    Design.createMeasurementsFile(oligoFilteredFiles, oligoMeasurementsFile,
        listMeasurements, null);
  }

  /**
   * In this phase, filter the measurements of the oligos.
   * @param listMeasurementFilters list of filter to apply
   * @param overvriteStatFile if stat file of filtered oligo must be override
   * @throws IOException if an error occurs while filtering
   */
  public void phase4FilterMeasurements(
      final List<MeasurementFilter> listMeasurementFilters,
      final boolean overvriteStatFile) throws IOException {

    final File filteredOligoMeasurementsFile =
        new File(this.outputDir, OLIGO_MEASUREMENTS_FILTERED_FILE);
    final File statsFile =
        new File(this.outputDir, OLIGO_MEASUREMENTS_FILTERED_STATS_FILE);

    Design.filterMeasurementsFile(new File(OLIGO_MEASUREMENTS_FILE),
        filteredOligoMeasurementsFile, overvriteStatFile ? statsFile : null,
        listMeasurementFilters);
  }

  /**
   * In this phase, select the oligos.
   * @param wSetter weight of selection
   * @throws IOException if an error occurs while selecting
   */
  public void phase5Select(final Select.WeightsSetter wSetter)
      throws IOException {

    final File filteredOligoMeasurementsFile =
        new File(this.outputDir, OLIGO_MEASUREMENTS_FILTERED_FILE);
    final File statsFile =
        new File(this.outputDir, OLIGO_MEASUREMENTS_FILTERED_STATS_FILE);
    final File selectedOligos = new File(this.outputDir, SELECTED_FILE);

    Select.select(filteredOligoMeasurementsFile, statsFile, selectedOligos,
        wSetter, this.windowSize);
  }

  //
  // Main method
  //

  public static void main(String[] args) throws IOException {

    new Design().run(args);

  }

  //
  // Developpement method
  //

  public void run(String[] args) throws IOException {

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

    boolean filterOnly = false;
    boolean seqFilter = true;
    boolean overvriteStatFile = true;

    if (!filterOnly) {

      // Create oligo sequences
      FastaOverlap.fastaOverlap(genomeFile, outputDir, OLIGO_SUFFIX,
          this.oligoLength);

      FastaOverlap.fastaOverlap(genomeMaskedFile, outputDir,
          OLIGO_MASKED_SUFFIX, this.oligoLength);

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
      measurements.add(new PositionMeasurement(this.windowSize));
      measurements.add(new TmMeasurement());
      measurements.add(new GCPencentMeasurement());
      measurements.add(new ComplexityMeasurement());
      measurements.add(new UnicityMeasurement(genomeFile, this.oligoLength,
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
    List<MeasurementFilter> filters = new ArrayList<MeasurementFilter>();
    filters.add(new FloatRangeFilter("%GC", 0, 1));
    filters.add(new FloatRangeFilter("Tm", 0, 100));
    filters.add(new FloatRangeFilter("Complexity", 0, 1));
    filters.add(new FloatRangeFilter("Unicity", 0, this.oligoLength));

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
        wSetter, this.windowSize);

  }

}
