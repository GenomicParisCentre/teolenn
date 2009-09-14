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

package fr.ens.transcriptome.teolenn.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import fr.ens.transcriptome.teolenn.DesignConstants;
import fr.ens.transcriptome.teolenn.Globals;
import fr.ens.transcriptome.teolenn.TeolennException;
import fr.ens.transcriptome.teolenn.measurement.Measurement;
import fr.ens.transcriptome.teolenn.measurement.filter.MeasurementFilter;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsIOFactory;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsReader;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsWriter;
import fr.ens.transcriptome.teolenn.sequence.SequenceIterator;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurementsStatWriter;

public class MeasurementCore {

  private static final Logger logger = Logger.getLogger(Globals.APP_NAME);

  /**
   * Create a measurement file.
   * @param inputFiles oligo input fasta files
   * @param measurementsFile output file
   * @param statsFile statFile to create (optional)
   * @throws IOException if an error occurs while creating the measurement
   */
  public static final void createMeasurementsFile(final List<File> inputFiles,
      final File measurementsFile, final List<Measurement> measurements,
      final File statsFile) throws IOException {

    if (inputFiles == null || inputFiles.size() == 0)
      return;

    final SequenceMeasurementsWriter smw =
        SequenceMeasurementsIOFactory
            .createSequenceMeasurementsWriter(measurementsFile);

    final SequenceMeasurements sm = new SequenceMeasurements();
    if (measurements != null)
      for (Measurement m : measurements)
        sm.addMesurement(m);

    int id = 0;

    for (File inputFile : inputFiles)
      id = createMeasurementsFile(inputFile, smw, sm, id, true);

    smw.close();

    // Create a stat file if needed
    if (statsFile != null) {
      logger.fine("Create measurement stats file.");
      SequenceMeasurementsStatWriter smsw =
          new SequenceMeasurementsStatWriter(statsFile);

      smsw.write(sm);
    }
    logger.info("Create " + id + " entries in measurement file.");
  }

  private static final int createMeasurementsFile(final File inputFile,
      final SequenceMeasurementsWriter smw, final SequenceMeasurements sm,
      final int idStart, final boolean addStats) throws IOException {

    final SequenceIterator si = new SequenceIterator(inputFile);

    // smw.writeHeader(sm);

    int count = idStart;

    for (Measurement m : sm.getMeasurements())
      m.setProperty(DesignConstants.CURRENT_OLIGO_FILE_PARAMETER_NAME,
          inputFile.getAbsolutePath());

    while (si.hasNext()) {

      si.next();
      sm.setId(++count);
      sm.setSequence(si);
      sm.calcMesurements();
      if (addStats)
        sm.addMesurementsToStats();
      smw.writeSequenceMesurement(sm);

    }

    return count;
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

      int count = -1;
      SequenceMeasurements sm = null;
      SequenceMeasurements last = null;

      while ((sm = smr.next(sm)) != null) {

        if (count == -1) {

          // Clear stats from calc measurement phase
          for (Measurement m : sm.getMeasurements())
            m.clear();
          count = 0;
        }

        boolean pass = true;

        for (MeasurementFilter filter : filters)
          if (!filter.accept(sm)) {
            pass = false;
            break;
          }

        if (pass) {
          sm.addMesurementsToStats();
          smw.writeSequenceMesurement(sm);
          count++;
        }

        last = sm;
      }

      smr.close();
      smw.close();

      logger.info(""
          + count + " entries found for measurement after filtering.");

      // Create a stat file if needed
      if (statsFile != null) {
        logger.fine("Write stats file for measurements.");
        SequenceMeasurementsStatWriter smsw =
            new SequenceMeasurementsStatWriter(statsFile);

        smsw.write(last);
      }
    } catch (IOException e) {

      throw new TeolennException("IO Error while filtering measurements: "
          + e.getMessage());
    }

  }

}
