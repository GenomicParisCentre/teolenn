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

public class OligoNameMeasurement implements Measurement {

  public Object calcMesurement(Sequence sequence) {

    return sequence.getName();
  }

  public String getDescription() {

    return "Get the name of the sequence";
  }

  public String getName() {

    return "Name";
  }

  public Object getType() {

    return String.class;
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

  public void setProperty(final String key, final String value) {

  }

  public void clear() {

  }

}
