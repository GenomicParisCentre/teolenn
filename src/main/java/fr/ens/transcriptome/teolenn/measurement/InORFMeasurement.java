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

package fr.ens.transcriptome.teolenn.measurement;

import java.io.IOException;
import java.util.Properties;

import fr.ens.transcriptome.teolenn.measurement.resource.ORFResource;
import fr.ens.transcriptome.teolenn.measurement.resource.ORFResource.ORF;
import fr.ens.transcriptome.teolenn.sequence.Sequence;

public class InORFMeasurement extends BooleanMeasurement {

  private ORFResource ressource;
  private Properties ressourceProperties = new Properties();

  /**
   * Calc the measurement of a sequence.
   * @param sequence the sequence to use for the measurement
   * @return an int value
   */
  protected boolean calcBooleanMeasurement(final Sequence sequence) {

    final ORF orf = this.ressource.getORf(sequence);

    return orf != null;
  }

  public String getDescription() {

    return "Test if the oligonucleotide is in an ORF";
  }

  public String getName() {

    return "inorf";
  }

  /**
   * Set a parameter for the filter.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  public void setInitParameter(final String key, final String value) {

    if (key == null || value == null)
      return;
    this.ressourceProperties.setProperty(key.toLowerCase(), value);
  }

  /**
   * Run the initialization phase of the parameter.
   * @throws IOException if an error occurs while the initialization phase
   */
  public void init() throws IOException {

    this.ressource = ORFResource.getRessource(this.ressourceProperties);
  }

}
