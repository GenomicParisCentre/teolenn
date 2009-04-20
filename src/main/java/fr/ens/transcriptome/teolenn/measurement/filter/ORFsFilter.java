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
import java.util.Properties;

import fr.ens.transcriptome.teolenn.Design;
import fr.ens.transcriptome.teolenn.TeolennException;
import fr.ens.transcriptome.teolenn.measurement.ChromosomeMeasurement;
import fr.ens.transcriptome.teolenn.measurement.OligoStartMeasurement;
import fr.ens.transcriptome.teolenn.measurement.resource.ORFResource;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;

public class ORFsFilter implements MeasurementFilter {

  /** Measurement filter name. */
  public static final String MEASUREMENT_FILTER_NAME = "orfsfilter";

  private ORFResource ressource;
  private Properties ressourceProperties = new Properties();
  private boolean first = true;
  private int colChromosome;
  private int colOligoStart;
  private int oligoLength;

  /**
   * Filter a SequenceMeasurements.
   * @param sm SequenceMeasurements to test
   * @return true if the test allow to keep SequenceMeasurements values
   * @throws TeolennException if an error occurs while testing a
   *           SequenceMeasurement
   */
  public boolean accept(final SequenceMeasurements sm) throws TeolennException {

    if (this.first) {

      this.colChromosome =
          sm.getIndexMeasurment(ChromosomeMeasurement.MEASUREMENT_NAME);

      if (colChromosome == -1)
        throw new TeolennException("The "
            + ChromosomeMeasurement.MEASUREMENT_NAME
            + " measurement is mandatory to use " + MEASUREMENT_FILTER_NAME
            + " filter.");

      this.colOligoStart =
          sm.getIndexMeasurment(OligoStartMeasurement.MEASUREMENT_NAME);

      if (colOligoStart == -1)
        throw new TeolennException("The "
            + OligoStartMeasurement.MEASUREMENT_NAME
            + " measurement is mandatory to use " + MEASUREMENT_FILTER_NAME
            + " filter.");

      this.first = false;
    }

    final Object[] values = sm.getArrayMeasurementValues();

    final String chr = (String) values[this.colChromosome];
    final int pos = (Integer) values[this.colOligoStart];

    final String key = chr + "\t" + pos + "\t" + this.oligoLength;

    return this.ressource.getORf(key) != null;
  }

  /**
   * Set a parameter for the filter.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  public void setInitParameter(final String key, final String value) {

    if (key == null || value == null)
      return;
    if (Design.OLIGO_LENGTH_PARAMETER_NAME.equals(key))
      this.oligoLength = Integer.parseInt(value);

    this.ressourceProperties.setProperty(key.toLowerCase(), value);
  }

  /**
   * Run the initialization phase of the parameter.
   * @throws TeolennException if an error occurs while the initialization phase
   */
  public void init() throws TeolennException {

    try {
      this.ressource = ORFResource.getRessource(this.ressourceProperties);
    } catch (IOException e) {

      throw new TeolennException(e);
    }
  }

}
