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

package fr.ens.transcriptome.teolenn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import fr.ens.transcriptome.teolenn.core.MeasurementCore;
import fr.ens.transcriptome.teolenn.core.SequenceCore;
import fr.ens.transcriptome.teolenn.measurement.Measurement;
import fr.ens.transcriptome.teolenn.measurement.filter.MeasurementFilter;
import fr.ens.transcriptome.teolenn.measurement.io.MultiSequenceMeasurementWriter;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsIOFactory;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsReader;
import fr.ens.transcriptome.teolenn.output.Output;
import fr.ens.transcriptome.teolenn.resource.ChromosomeNameResource;
import fr.ens.transcriptome.teolenn.resource.OligoSequenceResource;
import fr.ens.transcriptome.teolenn.selector.SequenceSelector;
import fr.ens.transcriptome.teolenn.sequence.SequenceIterator;
import fr.ens.transcriptome.teolenn.sequence.SequenceWriter;
import fr.ens.transcriptome.teolenn.sequence.filter.SequenceFilter;
import fr.ens.transcriptome.teolenn.util.FileUtils;
import fr.ens.transcriptome.teolenn.util.StringUtils;
import fr.ens.transcriptome.teolenn.util.SystemUtils;

/**
 * This class define the core class of Teolenn.
 * @author Laurent Jourdren
 */
public class DesignCommand extends Design {

  private static final Logger logger = Logger.getLogger(Globals.APP_NAME);

  private long startTimeCurrentPhase;
  private long startTimeDesign;

  //
  // Execute method
  //

  /**
   * Execute the design.
   * @throws TeolennException if an error occurs while executing the design
   */
  public void execute() throws TeolennException {

    // Start the computation
    phase0();

    if (!isSkipPhase1())
      phase1CreateAllOligos();
    else
      ChromosomeNameResource.getRessource(getOligosDir()).load();

    if (!isSkipPhase2())
      phase2FilterAllOligos(getSequenceFiltersList());

    // Don't skip this step, Add ons measurement need to be registered
    phase3CalcMeasurements(getMeasurementsList());

    if (!isSkipPhase4())
      phase4FilterMeasurements(getMeasurementFiltersList(), true);

    if (!isSkipPhase5())
      phase5Select(getSelector(), getWeightSetters());
  }

  //
  // Module parameters initialization
  //

  /**
   * Set the default values of the initialization parameters of a module.
   * @param module Module which parameters must be set
   */
  public void setDefaultModuleInitParameters(final Module module) {

    if (module == null)
      return;

    module.setInitParameter(DesignConstants.GENOME_FILE_PARAMETER_NAME,
        getGenomeFile().getAbsolutePath());
    module.setInitParameter(DesignConstants.GENOME_MASKED_FILE_PARAMETER_NAME,
        getGenomeMaskedFile().getAbsolutePath());
    module.setInitParameter(DesignConstants.OUTPUT_DIR_PARAMETER_NAME,
        getOutputDir().getAbsolutePath());
    module.setInitParameter(DesignConstants.OLIGO_LENGTH_PARAMETER_NAME,
        Integer.toString(getOligoLength()));
    module.setInitParameter(DesignConstants.EXTENSION_FILTER_PARAMETER_NAME,
        DesignConstants.OLIGO_SUFFIX);
    module.setInitParameter(DesignConstants.OLIGO_DIR_PARAMETER_NAME,
        getOligosDir().getAbsolutePath());
    module.setInitParameter(DesignConstants.TEMP_DIR_PARAMETER_NAME,
        getTempDir().getAbsolutePath());
    module.setInitParameter(DesignConstants.START_1_PARAMETER_NAME, Boolean
        .toString(isStart1()));
  }

  //
  // Tests for executing phases
  //

  private boolean isSkipPhase1() {

    return isSkipSequenceCreation() || isSkipMeasurementsComputation();
  }

  private boolean isSkipPhase2() {

    return isSkipMeasurementsComputation() || isSkipSequenceFilters();
  }

  private boolean isSkipPhase3() {

    return isSkipMeasurementsComputation();
  }

  private boolean isSkipPhase4() {

    return isSkipMeasurementsFilters();
  }

  public boolean isSkipPhase5() {

    return isSkipSelector();
  }

  /**
   * Filter oligos fasta files
   * @param oligoFiles input file
   * @param sequenceFilters filters to apply
   * @param maskedFiles filter masked files too
   * @throws IOException if an error occurs while filtering
   */
  private static final void filterSequencesFiles(final List<File> oligoFiles,
      final List<SequenceFilter> sequenceFilters, final boolean maskedFiles)
      throws IOException {

    int count = 0;

    for (File oligoFile : oligoFiles) {

      final String basename = StringUtils.basename(oligoFile.getAbsolutePath());

      final SequenceIterator si1 = new SequenceIterator(oligoFile);
      final SequenceIterator si2 =
          maskedFiles ? new SequenceIterator(new File(basename
              + DesignConstants.OLIGO_MASKED_SUFFIX)) : null;

      final SequenceWriter sw1 =
          new SequenceWriter(new File(basename
              + DesignConstants.OLIGO_FILTERED_SUFFIX));
      final SequenceWriter sw2 =
          maskedFiles ? new SequenceWriter(new File(basename
              + DesignConstants.OLIGO_MASKED_FILTERED_SUFFIX)) : null;

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
          count++;
        }

      }

      sw1.close();
      if (maskedFiles)
        sw2.close();
    }

    logger.info("" + count + " oligonucleotides after filtering");
  }

  /**
   * Count the number of oligonucleotides created.
   * @param chrOligo map with values for non maked genome
   * @return the number of oligonucleotides created
   */
  private int countOligosCreated(final Map<String, Integer> chrOligo) {

    if (chrOligo == null)
      return 0;

    int count = 0;

    for (Map.Entry<String, Integer> e : chrOligo.entrySet())
      count += e.getValue();

    return count;
  }

  /**
   * Verify that the names and the length of the chromosomes are the same in the
   * maked genome and the non masked genome.
   * @param chrOligo map with values for non maked genome
   * @param chrMasked map with values for non maked genome
   * @throws TeolennException if the name, the number or the length of the
   *           chromosomes are not the same
   */
  private void verifyChromosomeLengths(final Map<String, Integer> chrOligo,
      final Map<String, Integer> chrMasked) throws TeolennException {

    if (chrOligo == null)
      throw new TeolennException("chrOligo is null");
    if (chrMasked == null)
      throw new TeolennException("chrOligo is null");

    if (chrOligo.size() != chrMasked.size())
      throw new TeolennException(
          "The number of chromosomes in sequences masked and non masked are not the same.");

    for (Map.Entry<String, Integer> e : chrOligo.entrySet()) {

      final String key = e.getKey();

      if (!chrMasked.containsKey(key))
        throw new TeolennException("Unknown masked chromosome: " + key);
      if (chrMasked.get(key).intValue() != e.getValue().intValue())
        throw new TeolennException(
            "The length of the "
                + key
                + " chromosome is not the same in masked sequence masked and non masked : "
                + e.getValue() + " bp / " + chrMasked.get(key) + " bp (masked)");
    }

  }

  //
  // Phases methods
  // 

  /**
   * In this the initialization phase of the design.
   */
  public void phase0() throws TeolennException {

    logger.info("Date: " + new Date(System.currentTimeMillis()));
    logger.info("Host: " + SystemUtils.getHostName());
    logger.info("Operating system name: " + System.getProperty("os.name"));
    logger.info("Operating system arch: " + System.getProperty("os.arch"));
    logger
        .info("Operating system version: " + System.getProperty("os.version"));
    logger.info("Java version: " + System.getProperty("java.version"));
    logger.info("Log level: " + logger.getLevel());

    logger.info("Start position: " + (isStart1() ? "1" : "0"));
    logger.info("Oligo length: " + getOligoLength());
    logger.info("Genome file: " + getGenomeFile());
    logger.info("Genome masked file: " + getGenomeMaskedFile());
    logger.info("Output directory: " + getOutputDir());

    this.startTimeDesign = System.currentTimeMillis();

    if (!getOligosDir().exists())
      if (!getOligosDir().mkdirs())
        throw new TeolennException("Unable to create oligos directory.");

    if (!getTempDir().exists())
      if (!getTempDir().mkdirs())
        throw new TeolennException("Unable to create temporary directory.");

    // Clean temporary directory
    FileUtils.removeFiles(getTempDir().listFiles(), true);

    // Create resources
    OligoSequenceResource.getRessource(getOligosDir(),
        DesignConstants.OLIGO_SUFFIX, getOligoLength(), isStart1());
  }

  /**
   * In this phase, create all the oligos.
   * @throws IOException if an error occurs while creating all oligos
   */
  public void phase1CreateAllOligos() throws TeolennException {

    if (isSkipPhase1())
      return;

    logStartPhase("create oligos");

    FileUtils.removeFiles(getOligosDir().listFiles(), false);

    Map<String, Integer> chrOligo = null;
    Map<String, Integer> chrMasked = null;

    try {
      chrOligo =
          SequenceCore.fastaOverlap(getGenomeFile(), getOligosDir(),
              DesignConstants.OLIGO_SUFFIX, getOligoLength(), isStart1());

      if (isGenomeMaskedFile())
        chrMasked =
            SequenceCore.fastaOverlap(getGenomeMaskedFile(), getOligosDir(),
                DesignConstants.OLIGO_MASKED_SUFFIX, getOligoLength(),
                isStart1());
    } catch (IOException e) {
      throw new TeolennException(e);
    }

    if (isGenomeMaskedFile())
      verifyChromosomeLengths(chrOligo, chrMasked);

    // Create chromosome names resource
    ChromosomeNameResource chromosomeNames =
        ChromosomeNameResource.getRessource(getOligosDir());

    // Fill chromosome names and save chromosome list file
    chromosomeNames.addChromosomesNames(chrOligo.keySet());

    logger.info(""
        + countOligosCreated(chrOligo) + " oligos created in "
        + chrOligo.size() + " chromosomes.");

    logEndPhase("create oligos");
  }

  /**
   * In this phase, remove from the generated oligos all the invalid oligos.
   * @param listSequenceFilters list of sequence filters to apply
   * @throws IOException if an error occus while filtering
   */
  public void phase2FilterAllOligos(
      final List<SequenceFilter> listSequenceFilters) throws TeolennException {

    if (isSkipPhase2())
      return;

    logStartPhase("filter oligos");

    FileUtils.removeFiles(FileUtils.listFilesByExtension(getOligosDir(),
        DesignConstants.OLIGO_FILTERED_SUFFIX), false);
    FileUtils.removeFiles(FileUtils.listFilesByExtension(getOligosDir(),
        DesignConstants.OLIGO_MASKED_FILTERED_SUFFIX), false);

    // Init all the filters
    for (SequenceFilter sf : listSequenceFilters)
      sf.init();

    // Get the list of oligos files to process
    final List<String> chrNames =
        ChromosomeNameResource.getRessource().getChromosomesNames();
    final List<File> oligoFiles = new ArrayList<File>(chrNames.size());

    final File oligoDir = getOligosDir();
    for (String chrName : chrNames)
      oligoFiles
          .add(new File(oligoDir, chrName + DesignConstants.OLIGO_SUFFIX));

    try {
      DesignCommand.filterSequencesFiles(oligoFiles, listSequenceFilters,
          isGenomeMaskedFile());
    } catch (IOException e) {

      throw new TeolennException("Error while filtering sequence: "
          + e.getMessage());
    }

    logEndPhase("filter oligos");
  }

  /**
   * In this phase, compute all the measurements of the oligos.
   * @param listMeasurements list of measurements to compute
   * @throws TeolennException if an error occus while filtering
   */
  public void phase3CalcMeasurements(final List<Measurement> listMeasurements)
      throws TeolennException {

    if (isSkipPhase3())
      return;

    logStartPhase("calc measurements");

    // Initialize the measurements
    for (Measurement m : listMeasurements) {
      logger.fine("init measurement: " + m.getName());
      m.init();
    }

    // Get the list of filtered oligos files to process
    final List<String> chrNames =
        ChromosomeNameResource.getRessource().getChromosomesNames();
    final List<File> oligoFilteredFiles = new ArrayList<File>(chrNames.size());

    final File oligoDir = getOligosDir();
    final String suffix =
        isSkipSequenceFilters()
            ? DesignConstants.OLIGO_SUFFIX
            : DesignConstants.OLIGO_FILTERED_SUFFIX;

    for (String chrName : chrNames)
      oligoFilteredFiles.add(new File(oligoDir, chrName + suffix));

    // Test if input files exists
    if (oligoFilteredFiles == null || oligoFilteredFiles.size() == 0) {

      logger.severe("No file found for oligo measurement computation.");
      throw new RuntimeException(
          "No file found for oligo measurement computation.");
    }

    // Calc oligos measurements
    try {
      File oligoMeasurementsFile =
          new File(getOutputDir(), DesignConstants.OLIGO_MEASUREMENTS_FILE);

      File oligoStatsFile =
          new File(getOutputDir(),
              DesignConstants.OLIGO_MEASUREMENTS_STATS_FILE);

      MeasurementCore.createMeasurementsFile(oligoFilteredFiles,
          oligoMeasurementsFile, listMeasurements, oligoStatsFile);
    } catch (IOException e) {

      throw new TeolennException("Unable to create measurement file: "
          + e.getMessage());
    }

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
        new File(getOutputDir(),
            DesignConstants.OLIGO_MEASUREMENTS_FILTERED_FILE);
    final File statsFile =
        new File(getOutputDir(),
            DesignConstants.OLIGO_MEASUREMENTS_FILTERED_STATS_FILE);

    MeasurementCore.filterMeasurementsFile(new File(
        DesignConstants.OLIGO_MEASUREMENTS_FILE),
        filteredOligoMeasurementsFile, overvriteStatFile ? statsFile : null,
        listMeasurementFilters);

    logEndPhase("filter measurements");
  }

  /**
   * In this phase, select the oligos.
   * @param selector SequenceSelector to use
   * @param wSetter weight of selection
   * @throws TeolennException if an error occurs while selecting
   */
  public void phase5Select(final SequenceSelector selector,
      final WeightsSetter wSetter) throws TeolennException {

    logStartPhase("select");

    final File oligoMeasurementsFile =
        new File(getOutputDir(), DesignConstants.OLIGO_MEASUREMENTS_FILE);
    final File filteredOligoMeasurementsFile =
        new File(getOutputDir(),
            DesignConstants.OLIGO_MEASUREMENTS_FILTERED_FILE);
    final File statsFile =
        new File(getOutputDir(),
            DesignConstants.OLIGO_MEASUREMENTS_FILTERED_STATS_FILE);
    final File selectedOligos =
        new File(getOutputDir(), DesignConstants.SELECTED_FILE);

    if (!statsFile.exists()) {

      logger.severe("No stats file found.");
      throw new RuntimeException("No stats file found.");
    }

    logger.info("Use " + selector.getName() + " as selector.");

    // Set additional parameters
    selector.setInitParameter(DesignConstants.MEASUREMENT_FILE_PARAMETER_NAME,
        oligoMeasurementsFile.getAbsolutePath());
    selector.setInitParameter(
        DesignConstants.FILTERED_MEASUREMENT_FILE_PARAMETER_NAME,
        filteredOligoMeasurementsFile.getAbsolutePath());
    selector.setInitParameter(DesignConstants.STATS_FILE_PARAMETER_NAME,
        statsFile.getAbsolutePath());

    // Initialize the selector
    selector.init();

    try {

      // Open measurement file
      final SequenceMeasurementsReader measurementReader =
          SequenceMeasurementsIOFactory
              .createSequenceMeasurementsFilteredReader(
                  filteredOligoMeasurementsFile, oligoMeasurementsFile);

      // Open output file
      final MultiSequenceMeasurementWriter measurementWriter =
          new MultiSequenceMeasurementWriter();

      // Init the outputs and add it to measurementWriter
      for (Output o : getOutputList()) {
        o.setInitParameter(DesignConstants.OUTPUT_DEFAULT_FILE_PARAMETER_NAME,
            selectedOligos.getAbsolutePath());
        o.init();
        measurementWriter.addWriter(o);
      }

      // Launch selection
      selector.select(measurementReader, measurementWriter, wSetter);

    } catch (IOException e) {
      throw new TeolennException(e);
    }

    // Select.select(filteredOligoMeasurementsFile, statsFile, selectedOligos,
    // wSetter, this.windowLength, this.windowStep);

    logEndPhase("select");
    final long endTimeDesign = System.currentTimeMillis();
    logger.info("Process the design in "
        + StringUtils.toTimeHumanReadable(endTimeDesign - this.startTimeDesign)
        + " ms.");
  }

  //
  // Utility methods
  //

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
}
