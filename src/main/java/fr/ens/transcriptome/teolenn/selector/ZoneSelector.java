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

package fr.ens.transcriptome.teolenn.selector;

import java.io.IOException;
import java.util.Properties;

import fr.ens.transcriptome.teolenn.DesignConstants;
import fr.ens.transcriptome.teolenn.TeolennException;
import fr.ens.transcriptome.teolenn.measurement.ChromosomeMeasurement;
import fr.ens.transcriptome.teolenn.measurement.Measurement;
import fr.ens.transcriptome.teolenn.measurement.OligoStartMeasurement;
import fr.ens.transcriptome.teolenn.resource.ORFResource;
import fr.ens.transcriptome.teolenn.resource.ORFResource.ORF;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;

public class ZoneSelector extends SimpleSelector {

  // private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  private static final float MIN_SCORE = -1 * Float.MAX_VALUE;

  /** Selector name. */
  public static final String SELECTOR_NAME = "zone";

  private ORFResource ressource;
  private Properties ressourceProperties = new Properties();
  private int oligoLength;

  /**
   * Get the name of the selector.
   * @return the name of the selector
   */
  public String getName() {

    return SELECTOR_NAME;
  }

  /**
   * Get the description of the selector.
   * @return the description of the selector
   */
  public String getDescription() {

    return "A Selector for ORF design";
  }

  /**
   * Set a parameter for the selector.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  public void init() throws TeolennException {

    try {
      this.ressource = ORFResource.getRessource(this.ressourceProperties);
    } catch (IOException e) {

      throw new TeolennException(e);
    }

    addMeasurement(new From5PrimeORFMeasurement(this.ressource,
        this.oligoLength));
    addMeasurement(new FromStartORFMeasurement());
    addMeasurement(new FromEndORFMeasurement());
    addMeasurement(new ORFMeasurement());
  }

  /**
   * Set a parameter for the selector.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  public void setInitParameter(final String key, final String value) {

    if (key == null || value == null)
      return;
    if (DesignConstants.OLIGO_LENGTH_PARAMETER_NAME.equals(key))
      this.oligoLength = Integer.parseInt(value);

    this.ressourceProperties.setProperty(key.toLowerCase(), value);

    super.setInitParameter(key, value);
  }

  @Override
  public void doSelection() throws IOException {

    final ORFResource orfRessource = this.ressource;
    final int oligoLength = this.oligoLength;

    boolean first = true;
    int indexScaffold = -1;
    int indexStartPosition = -1;
    int indexORF = -1;
    int indexFromStart = -1;
    int indexFromEnd = -1;

    float bestScore = MIN_SCORE;
    int valuesLength = -1;

    // Object used to read oligo measurement
    SequenceMeasurements sm = null;
    Object[] values = null;

    final SequenceMeasurements smToWrite = new SequenceMeasurements();
    ORF lastORF = null;

    while ((sm = next()) != null) {

      if (first) {

        indexScaffold =
            sm.getIndexMeasurment(ChromosomeMeasurement.MEASUREMENT_NAME);
        indexStartPosition =
            sm.getIndexMeasurment(OligoStartMeasurement.MEASUREMENT_NAME);
        indexORF = sm.getIndexMeasurment(ORFMeasurement.MEASUREMENT_NAME);
         indexFromStart =
         sm.getIndexMeasurment(FromStartORFMeasurement.MEASUREMENT_NAME);
        indexFromEnd =
            sm.getIndexMeasurment(FromEndORFMeasurement.MEASUREMENT_NAME);
        values = sm.getArrayMeasurementValues();

        for (Measurement m : sm.getMeasurements())
          smToWrite.addMesurement(m);

        valuesLength = values.length;
        first = false;
      }

      // Get oligo features
      final String chromosome = (String) values[indexScaffold];
      final int pos = (Integer) values[indexStartPosition];
      final int id = sm.getId();

      final ORF orf = orfRessource.getORF(chromosome, pos, oligoLength);

      if (lastORF != null && (orf == null || !lastORF.equals(orf))) {

        writeSelectedSequenceMeasurements(smToWrite);
        lastORF = null;
        bestScore = MIN_SCORE;
      }

      // If oligo is not in an ORF process next oligo
      if (orf == null)
        continue;

      if (lastORF == null)
        lastORF = orf;

      final float score = sm.getScore();

      // New best score ?
      if (score > bestScore) {

        bestScore = score;
        smToWrite.setId(id);
        values[indexORF] = orf.toString();

        // Add the global score
        final Object[] valuesToWrite = new Object[valuesLength];
        System.arraycopy(values, 0, valuesToWrite, 0, valuesLength);
        valuesToWrite[indexFromStart] = distanceFromStart(orf, pos);
        valuesToWrite[indexFromEnd] = distanceFromEnd(orf, pos, oligoLength);

        smToWrite.setArrayMeasurementValues(valuesToWrite);
      }

    }

    if (lastORF != null)
      writeSelectedSequenceMeasurements(smToWrite);

    close();
  }

  private static final int distanceFromStart(final ORF orf, final int oligoPos) {

    return oligoPos - orf.start;
  }

  private static final int distanceFromEnd(final ORF orf, final int oligoPos,
      final int oligoLength) {

    return orf.end - oligoPos - oligoLength;
  }

}
