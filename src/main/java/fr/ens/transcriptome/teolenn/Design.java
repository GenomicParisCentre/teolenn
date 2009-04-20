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

package fr.ens.transcriptome.teolenn;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import fr.ens.transcriptome.teolenn.measurement.Measurement;
import fr.ens.transcriptome.teolenn.measurement.filter.MeasurementFilter;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsIOFactory;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsReader;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsWriter;
import fr.ens.transcriptome.teolenn.selector.SequenceSelector;
import fr.ens.transcriptome.teolenn.selector.TilingSelector;
import fr.ens.transcriptome.teolenn.sequence.FastaOverlap;
import fr.ens.transcriptome.teolenn.sequence.SequenceIterator;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurementsStatWriter;
import fr.ens.transcriptome.teolenn.sequence.SequenceWriter;
import fr.ens.transcriptome.teolenn.sequence.filter.SequenceFilter;
import fr.ens.transcriptome.teolenn.util.FileUtils;
import fr.ens.transcriptome.teolenn.util.StringUtils;

public class Design {

  private static final Logger logger = Logger.getLogger(Globals.APP_NAME);

  private static final int WINDOW_LEN_DEFAULT = 140; // 141;
  private static final int WINDOW_STEP_DEFAULT = 140;
  private static final int OLIGO_LEN_DEFAULT = 60;

  // private static final int UNICITY_MAX_PREFIX_LEN = 30;

  public static final String OLIGO_SUFFIX = ".oligo";
  public static final String OLIGO_MASKED_SUFFIX = ".masked";
  public static final String OLIGO_FILTERED_SUFFIX = ".filtered.oligo";
  public static final String OLIGO_MASKED_FILTERED_SUFFIX = ".filtered.masked";

  private static final String OLIGO_MEASUREMENTS_FILE = "oligo.mes";
  private static final String OLIGO_MEASUREMENTS_FILTERED_FILE = "filtered.mes";
  private static final String OLIGO_MEASUREMENTS_FILTERED_STATS_FILE =
      "filtered.stats";
  private static final String SELECTED_FILE = "select.mes";

  public static final String GENOME_FILE_PARAMETER_NAME = "_genomefile";
  public static final String GENOME_MASKED_FILE_PARAMETER_NAME =
      "_genomemaskedfile";
  public static final String OUTPUT_DIR_PARAMETER_NAME = "_outputdir";
  public static final String WINDOW_SIZE_PARAMETER_NAME = "_windowsize";
  public static final String OLIGO_LENGTH_PARAMETER_NAME = "_oligolength";
  public static final String EXTENSION_FILTER_PARAMETER_NAME = "_extensionfilter";

  private int windowLength = WINDOW_LEN_DEFAULT;
  private int oligoLength = OLIGO_LEN_DEFAULT;
  private int windowStep = WINDOW_STEP_DEFAULT;

  private File genomeFile;
  private File genomeMaskedFile;
  private File outputDir = (new File("")).getAbsoluteFile();

  private boolean skipSequenceFilters;
  private boolean skipMeasurementsComputation;
  private boolean skipMeasurementsFilters;

  private long startTimeCurrentPhase;
  private long startTimeDesign;

  //
  // Getters
  //

  /**
   * Get the window length of the design.
   * @return the window length
   */
  public int getWindowLength() {
    return windowLength;
  }

  /**
   * Get the window step of the design.
   * @return the window step
   */
  public int getWindowStep() {
    return windowStep;
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
   * Get the output directory
   * @return the output directory
   */
  public File getOutputDir() {
    return outputDir;
  }

  /**
   * Get if the sequences filters phase must be skipped.
   * @return true if the sequences filters phase must be skipped.
   */
  public boolean isSkipSequenceFilters() {
    return skipSequenceFilters;
  }

  /**
   * Get if the measurements computation phase must be skipped.
   * @return true if the measurement computation phase must be skipped.
   */
  public boolean isSkipMeasurementsComputation() {
    return skipMeasurementsComputation;
  }

  /**
   * Get if the measurements filters phase must be skipped.
   * @return true if the measurement computation phase must be skipped.
   */
  public boolean isSkipMeasurementsFilters() {
    return skipMeasurementsFilters;
  }

  /**
   * Test if genome masked file is set.
   * @return true if genome masked file is set
   */
  public boolean isGenomeMaskedFile() {

    return this.genomeMaskedFile != null;
  }

  //
  // Setters
  //

  /**
   * Set the window length.
   * @param windowLength the window length to set
   */
  public void setWindowLength(final int windowLength) {

    if (windowLength <= 0)
      throw new IllegalArgumentException("Invalid window length value: "
          + windowLength);

    this.windowLength = windowLength;
  }

  /**
   * Set the window step.
   * @param windowStep the window step to set
   */
  public void setWindowStep(final int windowStep) {

    if (windowStep <= 0)
      throw new IllegalArgumentException("Invalid window step value: "
          + windowStep);

    this.windowStep = windowStep;
  }

  /**
   * Set the olgo length
   * @param oligoLength The oligo length
   */
  public void setOligoLength(final int oligoLength) {

    if (oligoLength <= 0)
      throw new IllegalArgumentException("Invalid oligo length value: "
          + oligoLength);

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

  /**
   * Set if the sequences filters phase must be skipped.
   * @param skipSequenceFilters if the sequences filters phase must be skipped.
   */
  public void setSkipSequenceFilters(final boolean skipSequenceFilters) {
    this.skipSequenceFilters = skipSequenceFilters;
  }

  /**
   * Set if the measurement computation phase must be skipped.
   * @param skipMeasurementsComputation if the measurement computation phase
   *          must be skipped.
   */
  public void setSkipMeasurementsComputation(
      final boolean skipMeasurementsComputation) {
    this.skipMeasurementsComputation = skipMeasurementsComputation;
  }

  /**
   * Set if the measurement filter phase must be skipped.
   * @param skipMeasurementsFilters if the measurement filter phase must be
   *          skipped.
   */
  public void setSkipMeasurementsFilters(final boolean skipMeasurementsFilters) {
    this.skipMeasurementsFilters = skipMeasurementsFilters;
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
        SequenceMeasurementsIOFactory
            .createSequenceMeasurementsWriter(measurementsFile);

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
      SequenceMeasurementsStatWriter smsw =
          new SequenceMeasurementsStatWriter(statsFile);

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
   * @param maskedFiles filter masked files too
   * @throws IOException if an error occurs while filtering
   */
  public static final void filterSequencesFiles(final File[] oligoFiles,
      final List<SequenceFilter> sequenceFilters, final boolean maskedFiles)
      throws IOException {

    for (int i = 0; i < oligoFiles.length; i++) {

      final File oligoFile = oligoFiles[i];
      final String basename = StringUtils.basename(oligoFile.getAbsolutePath());

      final SequenceIterator si1 = new SequenceIterator(oligoFile);
      final SequenceIterator si2 =
          maskedFiles ? new SequenceIterator(new File(basename
              + OLIGO_MASKED_SUFFIX)) : null;

      final SequenceWriter sw1 =
          new SequenceWriter(new File(basename + OLIGO_FILTERED_SUFFIX));
      final SequenceWriter sw2 =
          maskedFiles ? new SequenceWriter(new File(basename
              + OLIGO_MASKED_FILTERED_SUFFIX)) : null;

      final SequenceFilter[] filters =
          sequenceFilters.toArray(new SequenceFilter[0]);

      while (si1.hasNext()) {

        si1.next();
        if (maskedFiles)
          si2.next();

        boolean result = true;

        for (int j = 0; j < filters.length; j++)
          if (!filters[j].accept(si1)) {
            result = false;
            break;
          }

        if (result) {
          sw1.write(si1);
          if (maskedFiles)
            sw2.write(si2);
        }

      }

      sw1.close();
      if (maskedFiles)
        sw2.close();
    }

  }

  /**
   * Filter a measurement file.
   * @param measurementsFile input file
   * @param filteredMeasurementsFile output file
   * @param statsFile statFile to create (optional)
   * @param filters Filters to applys
   * @throws TeolennException if an error occurs while filtering
   * @throws IOException if an error occurs while filtering
   */
  public static final void filterMeasurementsFile(final File measurementsFile,
      final File filteredMeasurementsFile, final File statsFile,
      final List<MeasurementFilter> filters) throws TeolennException {

    try {
      final SequenceMeasurementsReader smr =
          SequenceMeasurementsIOFactory
              .createSequenceMeasurementsReader(measurementsFile);

      final SequenceMeasurementsWriter smw =
          SequenceMeasurementsIOFactory
              .createSequenceMeasurementsFilteredWriter(
                  filteredMeasurementsFile, measurementsFile);

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
        }

        last = sm;
      }

      smr.close();
      smw.close();

      // Create a stat file if needed
      if (statsFile != null) {
        SequenceMeasurementsStatWriter smsw =
            new SequenceMeasurementsStatWriter(statsFile);

        smsw.write(last);
      }
    } catch (IOException e) {

      throw new TeolennException("IO Error while filtering measurements: "
          + e.getMessage());
    }

  }

  /**
   * Add log entry for start phase.
   * @param phaseName Name of current the phase
   */
  private void logStartPhase(final String phaseName) {

    this.startTimeCurrentPhase = System.currentTimeMillis();
    logger.info("Start " + phaseName + " phase.");
  }

  /**
   * Add log entry for end phase.
   * @param phaseName Name of current the phase
   */
  private void logEndPhase(final String phaseName) {

    final long endTimePhase = System.currentTimeMillis();

    logger.info("Process phase "
        + phaseName
        + " in "
        + StringUtils.toTimeHumanReadable(endTimePhase
            - this.startTimeCurrentPhase) + " ms.");
  }

  public boolean isSkipPhase1() {

    return isSkipMeasurementsComputation();
  }

  public boolean isSkipPhase2() {

    return isSkipMeasurementsComputation() || isSkipSequenceFilters();
  }

  public boolean isSkipPhase3() {

    return isSkipMeasurementsComputation();
  }

  public boolean isSkipPhase4() {

    return isSkipMeasurementsFilters();
  }

  /**
   * In this the initialization phase of the design.
   */
  public void phase0() throws IOException {

    logger.info("Java version: " + System.getProperty("java.version"));
    logger.info("Log level:" + logger.getLevel());
    logger.info("Window length: " + this.windowLength);
    logger.info("Window step: " + this.windowStep);
    logger.info("Oligo length: " + this.oligoLength);
    logger.info("Genome file: " + this.genomeFile);
    logger.info("Genome masked file: " + this.genomeMaskedFile);
    logger.info("Output directory: " + this.outputDir);

    this.startTimeDesign = System.currentTimeMillis();
  }

  /**
   * In this phase, create all the oligos.
   * @throws IOException if an error occurs while creating all oligos
   */
  public void phase1CreateAllOligos() throws IOException {

    if (isSkipPhase1())
      return;

    logStartPhase("create oligos");

    FastaOverlap.fastaOverlap(this.genomeFile, outputDir, Design.OLIGO_SUFFIX,
        this.oligoLength);

    if (isGenomeMaskedFile())
      FastaOverlap.fastaOverlap(this.genomeMaskedFile, outputDir,
          Design.OLIGO_MASKED_SUFFIX, this.oligoLength);

    logEndPhase("create oligos");
  }

  /**
   * In this phase, remove from the generated oligos all the invalid oligos.
   * @param listSequenceFilters list of sequence filters to apply
   * @throws IOException if an error occus while filtering
   */
  public void phase2FilterAllOligos(
      final List<SequenceFilter> listSequenceFilters) throws IOException {

    if (isSkipPhase2())
      return;

    logStartPhase("filter oligos");

    // Init all the filters
    for (SequenceFilter sf : listSequenceFilters)
      sf.init();

    // Get the list of oligos files to process
    final File[] oligoFiles =
        FileUtils.listFilesByExtension(this.outputDir, Design.OLIGO_SUFFIX);
    Arrays.sort(oligoFiles);

    Design.filterSequencesFiles(oligoFiles, listSequenceFilters,
        isGenomeMaskedFile());

    logEndPhase("filter oligos");
  }

  /**
   * In this phase, compute all the measurements of the oligos.
   * @param listMeasurements list of measurements to compute
   * @throws IOException if an error occus while filtering
   */
  public void phase3CalcMeasurements(final List<Measurement> listMeasurements)
      throws IOException {

    if (isSkipPhase3())
      return;

    logStartPhase("calc measurements");

    // Initialize the measurements
    for (Measurement m : listMeasurements) {
      logger.fine("init measurement: " + m.getName());
      m.init();
    }

    // Get the list of filtered oligos files to process
    final File[] oligoFilteredFiles =
        FileUtils.listFilesByExtension(this.outputDir, isSkipSequenceFilters()
            ? Design.OLIGO_SUFFIX : Design.OLIGO_FILTERED_SUFFIX);
    Arrays.sort(oligoFilteredFiles);

    // Test if input files exists
    if (oligoFilteredFiles == null || oligoFilteredFiles.length == 0) {

      logger.severe("No file found for oligo measurement computation.");
      throw new RuntimeException(
          "No file found for oligo measurement computation.");
    }

    // Calc oligos measurements
    File oligoMeasurementsFile = new File(outputDir, OLIGO_MEASUREMENTS_FILE);
    Design.createMeasurementsFile(oligoFilteredFiles, oligoMeasurementsFile,
        listMeasurements, null);

    logEndPhase("calc measurements");
  }

  /**
   * In this phase, filter the measurements of the oligos.
   * @param listMeasurementFilters list of filter to apply
   * @param overvriteStatFile if stat file of filtered oligo must be override
   * @throws IOException if an error occurs while filtering
   */
  public void phase4FilterMeasurements(
      final List<MeasurementFilter> listMeasurementFilters,
      final boolean overvriteStatFile) throws TeolennException {

    if (isSkipPhase4())
      return;

    logStartPhase("filter measurements");

    // Initialize the measurement filters
    for (MeasurementFilter mf : listMeasurementFilters)
      mf.init();

    final File filteredOligoMeasurementsFile =
        new File(this.outputDir, OLIGO_MEASUREMENTS_FILTERED_FILE);
    final File statsFile =
        new File(this.outputDir, OLIGO_MEASUREMENTS_FILTERED_STATS_FILE);

    Design.filterMeasurementsFile(new File(OLIGO_MEASUREMENTS_FILE),
        filteredOligoMeasurementsFile, overvriteStatFile ? statsFile : null,
        listMeasurementFilters);

    logEndPhase("filter measurements");
  }

  /**
   * In this phase, select the oligos.
   * @param wSetter weight of selection
   * @throws IOException if an error occurs while selecting
   */
  public void phase5Select(final WeightsSetter wSetter) throws IOException {

    logStartPhase("select");

    final File oligoMeasurementsFile =
        new File(this.outputDir, OLIGO_MEASUREMENTS_FILE);
    final File filteredOligoMeasurementsFile =
        new File(this.outputDir, OLIGO_MEASUREMENTS_FILTERED_FILE);
    final File statsFile =
        new File(this.outputDir, OLIGO_MEASUREMENTS_FILTERED_STATS_FILE);
    final File selectedOligos = new File(this.outputDir, SELECTED_FILE);

    if (!statsFile.exists()) {

      logger.severe("No stats file found.");
      throw new RuntimeException("No stats file found.");
    }

    SequenceSelector selector = new TilingSelector();
    selector.setInitParameter("_oriMesFile", oligoMeasurementsFile
        .getAbsolutePath());
    selector.setInitParameter("_filteredMesFile", filteredOligoMeasurementsFile
        .getAbsolutePath());
    selector.setInitParameter("_statsFile", statsFile.getAbsolutePath());
    selector
        .setInitParameter("_selectedFile", selectedOligos.getAbsolutePath());
    selector.setInitParameter("_windowlength", "" + this.windowLength);
    selector.setInitParameter("_windowStep", "" + this.windowStep);

    selector.init();

    selector.select(wSetter);

    // Select.select(filteredOligoMeasurementsFile, statsFile, selectedOligos,
    // wSetter, this.windowLength, this.windowStep);

    logEndPhase("select");
    final long endTimeDesign = System.currentTimeMillis();
    logger.info("Process the design in "
        + StringUtils.toTimeHumanReadable(endTimeDesign - this.startTimeDesign)
        + " ms.");
  }

}
