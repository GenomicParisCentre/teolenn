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

package fr.ens.transcriptome.oligo.measurement;

import java.util.Properties;

import fr.ens.transcriptome.oligo.Sequence;

public class OligoLengthMeasurement implements Measurement {

  public Object calcMesurement(Sequence sequence) {

    String seqName = sequence.getName();

    int startPos = seqName.indexOf(":subseq(");
    int startPos2 = seqName.indexOf(",", startPos);
    int endPos = seqName.indexOf(")", startPos2);

    return Integer.parseInt(seqName.substring(startPos2 + 1, endPos));
  }

  public void addLastMeasurementToStats() {
  }

  public String getDescription() {

    return "Get the length of the sequence";
  }

  public String getName() {

    return "Length";
  }

  public Object getType() {

    return Integer.class;
  }

  public Object parse(final String s) {

    if (s == null)
      return null;

    return new String(s);
  }

  public float getScore(final Object value) {

    return 0;
  }

  public Properties computeStatistics() {

    return null;
  }

  public void clear() {

  }

  public void setProperty(final String key, final String value) {

  }

}
