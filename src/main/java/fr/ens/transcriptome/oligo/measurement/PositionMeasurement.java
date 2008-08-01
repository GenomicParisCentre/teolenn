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

public class PositionMeasurement implements Measurement {

  private int windowSize = -1;
  private int windowBestPosition;
  private boolean first = true;

  public Object calcMesurement(Sequence sequence) {

    if (this.windowSize == -1)
      throw new RuntimeException("Window size is undefined.");

    String seqName = sequence.getName();

    final int startPos = seqName.indexOf(":subseq(");
    final int endPos1 = seqName.indexOf(",", startPos);

    final int seqStart =
        Integer.parseInt(seqName.substring(startPos + 8, endPos1));

    if (this.first) {

      final int endPos2 = seqName.indexOf(")", startPos);
      final int lenPos =
          Integer.parseInt(seqName.substring(endPos1 + 1, endPos2));

      this.windowBestPosition =
          (int) Math.ceil(((this.windowSize - lenPos) / 2.0f));
      this.first = false;
    }

    final int windowStart =
        (int) (Math.floor((float) seqStart / (float) this.windowSize)
            * this.windowSize - 1);
    final int oligo2Window = seqStart - windowStart;

    final float result =
        1 - (Math.abs(this.windowBestPosition - oligo2Window) / ((float) this.windowSize - (float) this.windowBestPosition));

    return result;
  }

  public String getDescription() {

    return "Get a position score for the sequence";
  }

  public String getName() {

    return "Position";
  }

  public Object getType() {

    return Float.class;
  }

  public Object parse(final String s) {

    if (s == null)
      return null;

    return Float.parseFloat(s);
  }

  public void addLastMeasurementToStats() {
  }

  public float getScore(final Object value) {

    return (Float) value;
  }

  public Properties computeStatistics() {

    return null;
  }

  public void clear() {

    this.first = false;
  }

  public void setProperty(final String key, final String value) {

  }

  //
  // Constructors
  //

  public PositionMeasurement() {
  }

  public PositionMeasurement(final int windowSize) {

    this.windowSize = windowSize;
  }

}
