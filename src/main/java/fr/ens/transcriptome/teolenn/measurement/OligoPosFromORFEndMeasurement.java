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

public class OligoPosFromORFEndMeasurement extends IntegerMeasurement {

  private ORFResource ressource;
  private Properties ressourceProperties = new Properties();

  /**
   * Calc the measurement of a sequence.
   * @param sequence the sequence to use for the measurement
   * @return an int value
   */
  protected int calcIntMeasurement(final Sequence sequence) {

    final ORF orf = this.ressource.getORf(sequence);

    if (orf == null)
      return -1;

    final String seqName = sequence.getName();

    int startPos = seqName.indexOf(":subseq(");
    int startPos2 = seqName.indexOf(",", startPos);
    int endPos = seqName.indexOf(")", startPos2);

    final int start =
        Integer.parseInt(seqName.substring(startPos + 8, startPos2));
    final int len = Integer.parseInt(seqName.substring(startPos2 + 1, endPos));

    return orf.end - start - len;
  }

  public String getDescription() {

    return "Get the position of the oligonucleotide from the start of the ORF";
  }

  public String getName() {

    return "OligoPosFromORFEnd";
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
