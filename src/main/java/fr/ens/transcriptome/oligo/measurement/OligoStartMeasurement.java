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

public class OligoStartMeasurement implements Measurement {

  public Object calcMesurement(Sequence sequence) {

    String seqName = sequence.getName();

    int startPos = seqName.indexOf(":subseq(");
    int endPos = seqName.indexOf(",", startPos);

    return Integer.parseInt(seqName.substring(startPos + 8, endPos));
  }

  public String getDescription() {

    return "Get the start position of the sequence";
  }

  public String getName() {

    return "Start";
  }

  public Object getType() {

    return Integer.class;
  }

  public Object parse(final String s) {

    if (s == null)
      return null;

    return new String(s);
  }

  public void addLastMeasurementToStats() {
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
