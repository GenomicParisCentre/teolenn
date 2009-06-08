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
import java.security.InvalidParameterException;
import java.util.List;

import fr.ens.transcriptome.teolenn.measurement.Measurement;
import fr.ens.transcriptome.teolenn.measurement.filter.MeasurementFilter;
import fr.ens.transcriptome.teolenn.selector.SequenceSelector;
import fr.ens.transcriptome.teolenn.sequence.filter.SequenceFilter;

public class Design {

  private int oligoLength = DesignConstants.OLIGO_LEN_DEFAULT;

  private File genomeFile;
  private File genomeMaskedFile;
  private File outputDir = (new File("")).getAbsoluteFile();
  private File oligosDir;
  private File tempDir;
  private boolean start1 = false;

  private boolean skipSequenceCreation;
  private boolean skipSequenceFilters;
  private boolean skipMeasurementsComputation;
  private boolean skipMeasurementsFilters;
  private boolean skipSelector;

  private List<SequenceFilter> sequenceFiltersList;
  private List<Measurement> measurementsList;
  private List<MeasurementFilter> measurementFiltersList;
  private SequenceSelector selector;
  private WeightsSetter weightSetters;

  //
  // Getters
  //

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
   * Get the output directory for oligos.
   * @return the output directory for oligos
   */
  public File getOligosDir() {

    if (this.oligosDir == null)
      this.oligosDir = new File(getOutputDir(), DesignConstants.OLIGO_SUBDIR);

    return this.oligosDir;
  }

  /**
   * Get the temporary directory.
   * @return the temporary directory
   */
  public File getTempDir() {

    if (this.tempDir == null)
      this.tempDir = new File(getOutputDir(), DesignConstants.TEMP_SUBDIR);

    return this.tempDir;
  }

  /**
   * Get the output directory
   * @return the output directory
   */
  public File getOutputDir() {
    return this.outputDir;
  }

  /**
   * Get if the sequences creation phase must be skipped.
   * @return true if the sequences creation phase must be skipped.
   */
  public boolean isSkipSequenceCreation() {
    return this.skipSequenceCreation;
  }

  /**
   * Get if the sequences filters phase must be skipped.
   * @return true if the sequences filters phase must be skipped.
   */
  public boolean isSkipSequenceFilters() {
    return this.skipSequenceFilters;
  }

  /**
   * Get if the measurements computation phase must be skipped.
   * @return true if the measurement computation phase must be skipped.
   */
  public boolean isSkipMeasurementsComputation() {
    return this.skipMeasurementsComputation;
  }

  /**
   * Get if the measurements filters phase must be skipped.
   * @return true if the measurement computation phase must be skipped.
   */
  public boolean isSkipMeasurementsFilters() {
    return this.skipMeasurementsFilters;
  }

  /**
   * Get if the selector phase must be skipped.
   * @return true if the selector computation phase must be skipped.
   */
  public boolean isSkipSelector() {
    return this.skipSelector;
  }

  /**
   * Test if genome masked file is set.
   * @return true if genome masked file is set
   */
  public boolean isGenomeMaskedFile() {

    return this.genomeMaskedFile != null;
  }

  /**
   * Test if the position of the sequences starts at 1 (and not at 0).
   * @return true if the position of the sequences starts at 1
   */
  public boolean isStart1() {
    return start1;
  }

  /**
   * Get the list of sequence filters to use for the design.
   * @return a list of sequence filters
   */
  public List<SequenceFilter> getSequenceFiltersList() {
    return sequenceFiltersList;
  }

  /**
   * Get the list of measurements to use for the design.
   * @return a list of measurements
   */
  public List<Measurement> getMeasurementsList() {
    return measurementsList;
  }

  /**
   * Get the list of measurements filter to use.
   * @return a list of measurement filters
   */
  public List<MeasurementFilter> getMeasurementFiltersList() {
    return measurementFiltersList;
  }

  /**
   * Get the selector to use for the design.
   * @return The selector to use for the design
   */
  public SequenceSelector getSelector() {
    return selector;
  }

  /**
   * Get the weight setters to use for the selector.
   * @return A WeightSetter object
   */
  public WeightsSetter getWeightSetters() {
    return weightSetters;
  }

  //
  // Setters
  //

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
   * Set if the sequences creation phase must be skipped.
   * @param skipSequenceCreation if the sequences creation phase must be
   *          skipped.
   */
  public void setSkipSequenceCreation(final boolean skipSequenceCreation) {
    this.skipSequenceCreation = skipSequenceCreation;
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

  /**
   * Set if the selector phase must be skipped.
   * @param skipSelector if the selector phase must be skipped.
   */
  public void setSkipSelector(final boolean skipSelector) {
    this.skipSelector = skipSelector;
  }

  /**
   * Set if the position of the sequences starts at 1 (and not at 0).
   * @param start1 true if the position of the sequences starts at 1
   */
  public void setStart1(final boolean start1) {

    this.start1 = start1;
  }

  /**
   * Set the directory for oligos sequences.
   * @param oligosDir The directory as a File object
   */
  public void setOligosDir(final File oligosDir) {
    this.oligosDir = oligosDir;
  }

  /**
   * Set the temporary directory for computations.
   * @param tempDir The directory as a File object
   */
  public void setTempDir(final File tempDir) {
    this.tempDir = tempDir;
  }

  public void setSequenceFiltersList(
      final List<SequenceFilter> sequenceFiltersList) {

    if (sequenceFiltersList == null)
      throw new InvalidParameterException(
          "The list of sequence filter can't be null.");

    this.sequenceFiltersList = sequenceFiltersList;
  }

  /**
   * Set the measurement list for the design.
   * @param measurementsList The list of measurement to set
   */
  public void setMeasurementsList(final List<Measurement> measurementsList) {

    if (measurementsList == null)
      throw new InvalidParameterException(
          "The list of measurements can't be null.");

    this.measurementsList = measurementsList;
  }

  /**
   * Set the list of measurement filters for the design.
   * @param measurementFilterList The list of measurements filters
   */
  public void setMeasurementFiltersList(
      final List<MeasurementFilter> measurementFilterList) {

    if (measurementFilterList == null)
      throw new InvalidParameterException(
          "The list of measurement filters can't be null.");

    this.measurementFiltersList = measurementFilterList;
  }

  /**
   * Set the selector to use for the design.
   * @param selector The selector to set
   */
  public void setSelector(final SequenceSelector selector) {

    if (selector == null)
      throw new InvalidParameterException("The selector can't be null.");

    this.selector = selector;
  }

  /**
   * Set the weightSetters for the selector.
   * @param weightSetters The weightSetters to set
   */
  public void setWeightSetters(final WeightsSetter weightSetters) {

    if (weightSetters == null)
      throw new InvalidParameterException("The weightsetter can't be null.");

    this.weightSetters = weightSetters;
  }

}
