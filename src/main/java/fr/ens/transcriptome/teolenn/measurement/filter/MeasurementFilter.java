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

package fr.ens.transcriptome.teolenn.measurement.filter;

import java.io.IOException;

import fr.ens.transcriptome.teolenn.SequenceMeasurements;

/**
 * This interface define filters on SequenceMeasurement.
 * @author Laurent Jourdren
 */
public interface MeasurementFilter {

  /**
   * Filter a SequenceMeasurements.
   * @param sm SequenceMeasurements to test
   * @return true if the test allow to keep SequenceMeasurements values
   */
  boolean accept(SequenceMeasurements sm);

  /**
   * Set a parameter for the filter.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  void setInitParameter(String key, String value);

  /**
   * Run the initialization phase of the parameter.
   * @throws IOException if an error occurs while the initialization phase
   */
  void init() throws IOException;

}
